/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appdevgrp6;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 *
 * @author user
 */
public class APPDEVGRP6 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        // Get the EntityManagerFactory
        EntityManagerFactory emf = EntityManagerFactoryUtil.getEntityManagerFactory();

        // Create EntityManager instances and perform database operations as needed
        EntityManager em1 = emf.createEntityManager();
        // ... Perform database operations using em1

        em1.close();
    }
    
}
