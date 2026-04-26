package com.kindergarten.kindergarten.compte;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests unitaires pour AuthService (façade GRASP Controller).
 *
 * CORRECTION des deux erreurs :
 *
 * Erreur 1 — "cannot find symbol class Compte" (ligne 95) Cause : AuthService
 * délègue à AccountService. @InjectMocks injectait CompteRepo + AuthoritiesRepo
 * dans AuthService, mais AuthService n'a plus ces champs — il a AccountService
 * + RoleService. Mockito ne trouvait pas de champ Compte à injecter. Fix : On
 * mocke AccountService et RoleService (dépendances directes de AuthService),
 * pas les repos (qui appartiennent à AccountService).
 *
 * Erreur 2 — "cannot find symbol existsByCompteEmail" (ligne 132) Cause : Le
 * mock était authoritiesRepo, mais AuthService n'appelle plus authoritiesRepo
 * directement — c'est AccountService qui le fait. Le stub portait sur un mock
 * qui n'était jamais invoqué. Fix : On stubbe accountService.activerCompte()
 * via doThrow/when au lieu de descendre dans les détails du repo.
 *
 * Exécution : mvn test -Dtest=AuthServiceTest
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService — Tests de délégation (GRASP Controller)")
public class AuthServiceTest {

    // ── Mocks des dépendances DIRECTES de AuthService ───────────────────
    @Mock
    private AccountService accountService;

    @Mock
    private RoleService roleService;

    @InjectMocks
    private AuthService authService;

    // ── Données de test ──────────────────────────────────────────────────
    private Compte testCompte;
    private final String testEmail = "user@test.com";
    private final String testPassword = "password123";

    @BeforeEach
    void setUp() {
        testCompte = new Compte();
        testCompte.setEmail(testEmail);
        testCompte.setPassword("hashed_password");
        testCompte.setEnabled(false);
        // setType() supprimé (SRP) — le rôle est dans Authorities
    }

    // ==================== creerCompte ====================
    @Test
    @DisplayName("creerCompte — délègue à AccountService et retourne le compte")
    void testCreerCompte() {
        when(accountService.creerCompte(testEmail, testPassword, "Parent"))
                .thenReturn(testCompte);

        Compte result = authService.creerCompte(testEmail, testPassword, "Parent");

        assertNotNull(result);
        assertEquals(testEmail, result.getEmail());
        assertFalse(result.isEnabled(), "Le compte doit être désactivé à la création");
        verify(accountService, times(1)).creerCompte(testEmail, testPassword, "Parent");
    }

    @Test
    @DisplayName("creerCompte — délègue à AccountService pour le type Admin")
    void testCreerCompteAdmin() {
        Compte adminCompte = new Compte();
        adminCompte.setEmail(testEmail);
        adminCompte.setPassword("hashed");
        adminCompte.setEnabled(false);

        when(accountService.creerCompte(testEmail, testPassword, "Admin"))
                .thenReturn(adminCompte);

        Compte result = authService.creerCompte(testEmail, testPassword, "Admin");

        assertNotNull(result);
        assertFalse(result.isEnabled());
        verify(accountService, times(1)).creerCompte(testEmail, testPassword, "Admin");
    }

    // ==================== activerCompte ====================
    @Test
    @DisplayName("activerCompte — délègue à AccountService")
    void testActiverCompte() {
        // FIX erreur 2 : on vérifie la délégation, pas les détails du repo
        authService.activerCompte(testEmail);

        verify(accountService, times(1)).activerCompte(testEmail);
    }

    @Test
    @DisplayName("activerCompte — N'écrase pas une autorité déjà existante (délégation)")
    void testActiverCompteAvecAutoriteExistante() {
        // AccountService gère ce cas en interne — AuthService délègue simplement
        authService.activerCompte(testEmail);

        verify(accountService, times(1)).activerCompte(testEmail);
    }

    @Test
    @DisplayName("activerCompte — propage IllegalArgumentException si compte introuvable")
    void testActiverCompteNonExistant() {
        // FIX erreur 2 : on stubbe accountService, pas authoritiesRepo
        doThrow(new IllegalArgumentException("Compte introuvable : " + testEmail))
                .when(accountService).activerCompte(testEmail);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> authService.activerCompte(testEmail)
        );
        assertTrue(ex.getMessage().contains("Compte introuvable"));
        verify(accountService, times(1)).activerCompte(testEmail);
    }

    // ==================== desactiverCompte ====================
    @Test
    @DisplayName("desactiverCompte — délègue à AccountService")
    void testDesactiverCompte() {
        authService.desactiverCompte(testEmail);

        verify(accountService, times(1)).desactiverCompte(testEmail);
    }

    @Test
    @DisplayName("desactiverCompte — propage IllegalArgumentException si compte introuvable")
    void testDesactiverCompteNonExistant() {
        doThrow(new IllegalArgumentException("Compte introuvable : " + testEmail))
                .when(accountService).desactiverCompte(testEmail);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> authService.desactiverCompte(testEmail)
        );
        assertTrue(ex.getMessage().contains("Compte introuvable"));
    }

    // ==================== changerMotDePasse ====================
    @Test
    @DisplayName("changerMotDePasse — délègue à AccountService")
    void testChangerMotDePasse() {
        String nouveauMdp = "newPassword456";

        authService.changerMotDePasse(testEmail, nouveauMdp);

        verify(accountService, times(1)).changerMotDePasse(testEmail, nouveauMdp);
    }

    @Test
    @DisplayName("changerMotDePasse — propage IllegalArgumentException si compte introuvable")
    void testChangerMotDePasseNonExistant() {
        doThrow(new IllegalArgumentException("Compte introuvable : " + testEmail))
                .when(accountService).changerMotDePasse(eq(testEmail), anyString());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> authService.changerMotDePasse(testEmail, "newPassword")
        );
        assertTrue(ex.getMessage().contains("Compte introuvable"));
    }

    // ==================== getCurrentUser ====================
    @Test
    @DisplayName("getCurrentUser — délègue à AccountService et retourne le compte")
    void testGetCurrentUser() {
        java.security.Principal principal = () -> testEmail;
        when(accountService.getCurrentUser(principal)).thenReturn(testCompte);

        Compte result = authService.getCurrentUser(principal);

        assertNotNull(result);
        assertEquals(testEmail, result.getEmail());
        verify(accountService, times(1)).getCurrentUser(principal);
    }

    @Test
    @DisplayName("getCurrentUser — retourne null si principal est null")
    void testGetCurrentUserNull() {
        when(accountService.getCurrentUser(null)).thenReturn(null);

        Compte result = authService.getCurrentUser(null);

        assertNull(result);
        verify(accountService, times(1)).getCurrentUser(null);
    }

    @Test
    @DisplayName("getCurrentUser — retourne null si utilisateur introuvable en base")
    void testGetCurrentUserNonExistant() {
        java.security.Principal principal = () -> testEmail;
        when(accountService.getCurrentUser(principal)).thenReturn(null);

        Compte result = authService.getCurrentUser(principal);

        assertNull(result);
    }

    // ==================== getCompteByEmail ====================
    @Test
    @DisplayName("getCompteByEmail — délègue à AccountService et retourne Optional")
    void testGetCompteByEmail() {
        when(accountService.getCompteByEmail(testEmail))
                .thenReturn(Optional.of(testCompte));

        Optional<Compte> result = authService.getCompteByEmail(testEmail);

        assertTrue(result.isPresent());
        assertEquals(testCompte, result.get());
    }

    @Test
    @DisplayName("getCompteByEmail — retourne Optional.empty() si non trouvé")
    void testGetCompteByEmailNonExistant() {
        when(accountService.getCompteByEmail(testEmail))
                .thenReturn(Optional.empty());

        Optional<Compte> result = authService.getCompteByEmail(testEmail);

        assertFalse(result.isPresent());
    }

    // ==================== Helpers de rôle ====================
    // AuthService délègue à AccountService — on vérifie uniquement la délégation.
    // Les tests détaillés (mock authoritiesRepo) sont dans AccountServiceTest.
    @Test
    @DisplayName("hasRole — délègue à AccountService et retourne true")
    void testHasRoleTrue() {
        when(accountService.hasRole(testCompte, "Parent")).thenReturn(true);

        boolean result = authService.hasRole(testCompte, "Parent");

        assertTrue(result);
        verify(accountService, times(1)).hasRole(testCompte, "Parent");
    }

    @Test
    @DisplayName("hasRole — délègue à AccountService et retourne false")
    void testHasRoleFalse() {
        when(accountService.hasRole(testCompte, "Admin")).thenReturn(false);

        assertFalse(authService.hasRole(testCompte, "Admin"));
    }

    @Test
    @DisplayName("hasRole — retourne false si compte null")
    void testHasRoleNull() {
        when(accountService.hasRole(null, "Parent")).thenReturn(false);

        boolean result = authService.hasRole(null, "Parent");

        assertFalse(result);
        verify(accountService, times(1)).hasRole(null, "Parent");
    }

    @Test
    @DisplayName("isAdmin — délègue à AccountService et retourne true")
    void testIsAdminTrue() {
        when(accountService.isAdmin(testCompte)).thenReturn(true);

        assertTrue(authService.isAdmin(testCompte));
        verify(accountService, times(1)).isAdmin(testCompte);
    }

    @Test
    @DisplayName("isAdmin — délègue à AccountService et retourne false")
    void testIsAdminFalse() {
        when(accountService.isAdmin(testCompte)).thenReturn(false);

        assertFalse(authService.isAdmin(testCompte));
    }

    @Test
    @DisplayName("isDirector — délègue à AccountService et retourne true")
    void testIsDirectorTrue() {
        when(accountService.isDirector(testCompte)).thenReturn(true);

        assertTrue(authService.isDirector(testCompte));
        verify(accountService, times(1)).isDirector(testCompte);
    }

    @Test
    @DisplayName("isDirector — délègue à AccountService et retourne false")
    void testIsDirectorFalse() {
        when(accountService.isDirector(testCompte)).thenReturn(false);

        assertFalse(authService.isDirector(testCompte));
    }

    @Test
    @DisplayName("isParent — délègue à AccountService et retourne true")
    void testIsParentTrue() {
        when(accountService.isParent(testCompte)).thenReturn(true);

        assertTrue(authService.isParent(testCompte));
        verify(accountService, times(1)).isParent(testCompte);
    }

    @Test
    @DisplayName("isParent — délègue à AccountService et retourne false")
    void testIsParentFalse() {
        when(accountService.isParent(testCompte)).thenReturn(false);

        assertFalse(authService.isParent(testCompte));
    }
}
