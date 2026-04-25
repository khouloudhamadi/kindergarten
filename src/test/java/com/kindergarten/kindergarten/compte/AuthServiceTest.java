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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Tests unitaires pour AuthService
 *
 * Patterns testés : - Pattern GRASP Controller (délégation) - Isolation des
 * dépendances (mocks) - Vérification des droits d'accès - Gestion des erreurs
 *
 * Exécution : mvn test -Dtest=AuthServiceTest
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService - Tests unitaires")
public class AuthServiceTest {

    @Mock
    private CompteRepo compteRepo;

    @Mock
    private AuthoritiesRepo authoritiesRepo;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    // ---------------------------------------------------------------
    // Données de test partagées
    // ---------------------------------------------------------------
    private Compte testCompte;
    private final String testEmail = "user@test.com";
    private final String testPassword = "password123";

    /**
     * Initialisation avant chaque test.
     *
     * FIX #1 — setType() a été supprimé de Compte (SRP : le rôle appartient
     * désormais à Authorities / RoleService). On initialise uniquement les
     * champs qui existent encore dans Compte : email, password, enabled.
     *
     * Le rôle est testé via hasRole()/isAdmin()/isParent()/isDirector() dont la
     * logique interne s'appuie sur AuthoritiesRepo, pas sur Compte.getType().
     */
    @BeforeEach
    void setUp() {
        testCompte = new Compte();
        testCompte.setEmail(testEmail);
        testCompte.setPassword("hashed_password");
        testCompte.setEnabled(false);
        // setType() supprimé : le type/rôle est dans Authorities (SRP)
    }

    // ==================== Tests : creerCompte ====================
    @Test
    @DisplayName("creerCompte - Crée un compte désactivé avec mot de passe chiffré")
    void testCreerCompte() {
        // Arrange
        String encodedPassword = "$2b$10$hashed...";
        when(passwordEncoder.encode(testPassword)).thenReturn(encodedPassword);
        when(compteRepo.save(any(Compte.class))).thenReturn(testCompte);

        // Act
        // FIX #1 — creerCompte() accepte toujours le type en paramètre pour
        // créer l'entrée dans Authorities ; le type n'est plus stocké dans Compte.
        Compte result = authService.creerCompte(testEmail, testPassword, "Parent");

        // Assert
        assertNotNull(result);
        assertEquals(testEmail, result.getEmail());
        assertFalse(result.isEnabled(), "Le compte doit être désactivé à la création");

        verify(passwordEncoder, times(1)).encode(testPassword);
        verify(compteRepo, times(1)).save(any(Compte.class));
    }

    @Test
    @DisplayName("creerCompte - Fonctionne pour le type Admin")
    void testCreerCompteAdmin() {
        // Arrange
        // FIX #1 — on ne crée plus un Compte avec setType("Admin") car
        // setType() n'existe plus.  On retourne simplement un Compte valide.
        Compte adminCompte = new Compte();
        adminCompte.setEmail(testEmail);
        adminCompte.setPassword("hashed");
        adminCompte.setEnabled(false);

        when(passwordEncoder.encode(testPassword)).thenReturn("hashed");
        when(compteRepo.save(any(Compte.class))).thenReturn(adminCompte);

        // Act
        Compte result = authService.creerCompte(testEmail, testPassword, "Admin");

        // Assert
        assertNotNull(result);
        assertFalse(result.isEnabled(), "Le compte Admin doit être désactivé à la création");
        // Le rôle "Admin" est géré dans Authorities, pas dans Compte.
        verify(compteRepo, times(1)).save(any(Compte.class));
    }

    // ==================== Tests : activerCompte ====================
    @Test
    @DisplayName("activerCompte - Active le compte et crée l'autorité")
    void testActiverCompte() {
        // Arrange
        when(compteRepo.findById(testEmail)).thenReturn(Optional.of(testCompte));

        // FIX #2 — AuthoritiesRepo utilise maintenant un @Id Long (généré).
        // Il n'existe plus de existsById(String).
        // On utilise existsByCompteEmail(String) qui vérifie via le lien ManyToOne.
        when(authoritiesRepo.existsByCompteEmail(testEmail)).thenReturn(false);

        // Act
        authService.activerCompte(testEmail);

        // Assert
        assertTrue(testCompte.isEnabled(), "Le compte doit être activé");
        verify(authoritiesRepo, times(1)).save(any(Authorities.class));
        verify(compteRepo, times(1)).save(testCompte);
    }

    @Test
    @DisplayName("activerCompte - N'écrase pas une autorité déjà existante")
    void testActiverCompteAvecAutoriteExistante() {
        // Arrange
        when(compteRepo.findById(testEmail)).thenReturn(Optional.of(testCompte));

        // FIX #2 — même correction : existsByCompteEmail au lieu de existsById
        when(authoritiesRepo.existsByCompteEmail(testEmail)).thenReturn(true);

        // Act
        authService.activerCompte(testEmail);

        // Assert
        assertTrue(testCompte.isEnabled());
        // L'autorité existait déjà → on ne doit PAS en créer une nouvelle
        verify(authoritiesRepo, times(0)).save(any(Authorities.class));
    }

    @Test
    @DisplayName("activerCompte - Lève IllegalArgumentException si le compte n'existe pas")
    void testActiverCompteNonExistant() {
        // Arrange
        when(compteRepo.findById(testEmail)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> authService.activerCompte(testEmail)
        );
        assertTrue(ex.getMessage().contains("Compte introuvable"));
        verify(compteRepo, times(0)).save(any());
    }

    // ==================== Tests : desactiverCompte ====================
    @Test
    @DisplayName("desactiverCompte - Désactive un compte actif")
    void testDesactiverCompte() {
        // Arrange
        testCompte.setEnabled(true);
        when(compteRepo.findById(testEmail)).thenReturn(Optional.of(testCompte));

        // Act
        authService.desactiverCompte(testEmail);

        // Assert
        assertFalse(testCompte.isEnabled(), "Le compte doit être désactivé");
        verify(compteRepo, times(1)).save(testCompte);
    }

    @Test
    @DisplayName("desactiverCompte - Lève IllegalArgumentException si le compte n'existe pas")
    void testDesactiverCompteNonExistant() {
        // Arrange
        when(compteRepo.findById(testEmail)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> authService.desactiverCompte(testEmail)
        );
        assertTrue(ex.getMessage().contains("Compte introuvable"));
    }

    // ==================== Tests : changerMotDePasse ====================
    @Test
    @DisplayName("changerMotDePasse - Encode et sauvegarde le nouveau mot de passe")
    void testChangerMotDePasse() {
        // Arrange
        String nouveauMdp = "newPassword456";
        String nouveauMdpEncode = "$2b$10$newHashed...";

        when(compteRepo.findById(testEmail)).thenReturn(Optional.of(testCompte));
        when(passwordEncoder.encode(nouveauMdp)).thenReturn(nouveauMdpEncode);

        // Act
        authService.changerMotDePasse(testEmail, nouveauMdp);

        // Assert
        assertEquals(nouveauMdpEncode, testCompte.getPassword());
        verify(passwordEncoder, times(1)).encode(nouveauMdp);
        verify(compteRepo, times(1)).save(testCompte);
    }

    @Test
    @DisplayName("changerMotDePasse - Lève IllegalArgumentException si le compte n'existe pas")
    void testChangerMotDePasseNonExistant() {
        // Arrange
        when(compteRepo.findById(testEmail)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> authService.changerMotDePasse(testEmail, "newPassword")
        );
        assertTrue(ex.getMessage().contains("Compte introuvable"));
    }

    // ==================== Tests : getCurrentUser ====================
    @Test
    @DisplayName("getCurrentUser - Retourne l'utilisateur connecté")
    void testGetCurrentUser() {
        // Arrange
        java.security.Principal principal = () -> testEmail;
        when(compteRepo.findById(testEmail)).thenReturn(Optional.of(testCompte));

        // Act
        Compte result = authService.getCurrentUser(principal);

        // Assert
        assertNotNull(result);
        assertEquals(testEmail, result.getEmail());
    }

    @Test
    @DisplayName("getCurrentUser - Retourne null si principal est null")
    void testGetCurrentUserNull() {
        // Act
        Compte result = authService.getCurrentUser(null);

        // Assert
        assertNull(result);
        verify(compteRepo, times(0)).findById(anyString());
    }

    @Test
    @DisplayName("getCurrentUser - Retourne null si l'utilisateur n'existe pas en base")
    void testGetCurrentUserNonExistant() {
        // Arrange
        java.security.Principal principal = () -> testEmail;
        when(compteRepo.findById(testEmail)).thenReturn(Optional.empty());

        // Act
        Compte result = authService.getCurrentUser(principal);

        // Assert
        assertNull(result);
    }

    // ==================== Tests : getCompteByEmail ====================
    @Test
    @DisplayName("getCompteByEmail - Retourne un compte existant")
    void testGetCompteByEmail() {
        // Arrange
        when(compteRepo.findById(testEmail)).thenReturn(Optional.of(testCompte));

        // Act
        Optional<Compte> result = authService.getCompteByEmail(testEmail);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testCompte, result.get());
    }

    @Test
    @DisplayName("getCompteByEmail - Retourne Optional.empty() si non trouvé")
    void testGetCompteByEmailNonExistant() {
        // Arrange
        when(compteRepo.findById(testEmail)).thenReturn(Optional.empty());

        // Act
        Optional<Compte> result = authService.getCompteByEmail(testEmail);

        // Assert
        assertFalse(result.isPresent());
    }

    // ==================== Tests : vérification des rôles ====================
    // FIX #1 — les méthodes hasRole / isAdmin / isParent / isDirector ne
    // peuvent plus lire Compte.getType() (supprimé par SRP).
    // Deux stratégies possibles selon ton implémentation de AuthService :
    //
    //   Stratégie A — AuthService consulte AuthoritiesRepo (recommandé SRP) :
    //     → mocker authoritiesRepo.existsByCompteEmailAndAuthority(email, role)
    //
    //   Stratégie B — AuthService garde des helpers qui interrogent Authorities
    //     via un appel interne ; le test reste identique côté API publique.
    //
    // Les tests ci-dessous utilisent la stratégie A.
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("hasRole - Retourne true si l'autorité existe en base")
    void testHasRoleTrue() {
        // Arrange — mock AuthoritiesRepo pour simuler le rôle "Parent"
        when(authoritiesRepo.existsByCompteEmailAndAuthority(
                testEmail, RoleType.ROLE_PARENT)).thenReturn(true);

        // Act
        boolean result = authService.hasRole(testCompte, "Parent");

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("hasRole - Retourne false si l'autorité n'existe pas")
    void testHasRoleFalse() {
        // Arrange
        when(authoritiesRepo.existsByCompteEmailAndAuthority(
                testEmail, RoleType.ROLE_ADMIN)).thenReturn(false);

        // Act
        boolean result = authService.hasRole(testCompte, "Admin");

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("hasRole - Retourne false si le compte est null")
    void testHasRoleNull() {
        // Act — pas de mock nécessaire : null court-circuite avant le repo
        boolean result = authService.hasRole(null, "Parent");

        // Assert
        assertFalse(result);
        verify(authoritiesRepo, times(0))
                .existsByCompteEmailAndAuthority(anyString(), any());
    }

    @Test
    @DisplayName("isAdmin - Retourne true quand le rôle ROLE_ADMIN est présent")
    void testIsAdminTrue() {
        // Arrange
        when(authoritiesRepo.existsByCompteEmailAndAuthority(
                testEmail, RoleType.ROLE_ADMIN)).thenReturn(true);

        // Act
        boolean result = authService.isAdmin(testCompte);

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("isAdmin - Retourne false pour un compte sans rôle Admin")
    void testIsAdminFalse() {
        // Arrange
        when(authoritiesRepo.existsByCompteEmailAndAuthority(
                testEmail, RoleType.ROLE_ADMIN)).thenReturn(false);

        // Act
        boolean result = authService.isAdmin(testCompte);

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("isDirector - Retourne true quand le rôle ROLE_DIRECTOR est présent")
    void testIsDirectorTrue() {
        // Arrange
        when(authoritiesRepo.existsByCompteEmailAndAuthority(
                testEmail, RoleType.ROLE_DIRECTOR)).thenReturn(true);

        // Act
        boolean result = authService.isDirector(testCompte);

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("isDirector - Retourne false pour un compte sans rôle Director")
    void testIsDirectorFalse() {
        // Arrange
        when(authoritiesRepo.existsByCompteEmailAndAuthority(
                testEmail, RoleType.ROLE_DIRECTOR)).thenReturn(false);

        // Act
        boolean result = authService.isDirector(testCompte);

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("isParent - Retourne true quand le rôle ROLE_PARENT est présent")
    void testIsParentTrue() {
        // Arrange
        when(authoritiesRepo.existsByCompteEmailAndAuthority(
                testEmail, RoleType.ROLE_PARENT)).thenReturn(true);

        // Act
        boolean result = authService.isParent(testCompte);

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("isParent - Retourne false pour un compte sans rôle Parent")
    void testIsParentFalse() {
        // Arrange
        when(authoritiesRepo.existsByCompteEmailAndAuthority(
                testEmail, RoleType.ROLE_PARENT)).thenReturn(false);

        // Act
        boolean result = authService.isParent(testCompte);

        // Assert
        assertFalse(result);
    }
}
