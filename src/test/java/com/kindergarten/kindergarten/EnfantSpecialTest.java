package com.kindergarten.kindergarten;

import org.junit.jupiter.api.Test;
import com.kindergarten.kindergarten.parent.Enfant;
import com.kindergarten.kindergarten.parent.EnfantSpecial;
import static org.junit.jupiter.api.Assertions.*;

public class EnfantSpecialTest {

    @Test
    public void testEnfantSpecialHeriteDEnfant() {

        // --- Création ---
        EnfantSpecial es = new EnfantSpecial();
        es.setNom("Ben Ali");
        es.setPrenom("Sara");
        es.setSexe("F");
        es.setDatenais("2020-05-10");
        es.setEtatsante("Bonne");
        es.setTypeHandicap("Visuel");
        es.setSuiviMedical("Orthoptiste");

        // --- Test 1 : héritage ---
        assertTrue(es instanceof Enfant);

        // --- Test 2 : attributs hérités ---
        assertEquals("Ben Ali", es.getNom());
        assertEquals("Sara", es.getPrenom());

        // --- Test 3 : nouveaux attributs ---
        assertEquals("Visuel", es.getTypeHandicap());
        assertEquals("Orthoptiste", es.getSuiviMedical());

        // --- Test 4 : polymorphisme ---
        Enfant e = es;
        assertEquals("Ben Ali", e.getNom());

        System.out.println("✅ OCP respecté");
        System.out.println("✅ Attributs hérités : " + es.getNom() + " " + es.getPrenom());
        System.out.println("✅ Nouveaux attributs : " + es.getTypeHandicap() + " / " + es.getSuiviMedical());
    }
}