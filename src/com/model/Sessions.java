/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.model;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author PC
 */
@Entity
@Table(name = "sessions")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Sessions.findAll", query = "SELECT s FROM Sessions s"),
    @NamedQuery(name = "Sessions.findBySessionID", query = "SELECT s FROM Sessions s WHERE s.sessionID = :sessionID"),
    @NamedQuery(name = "Sessions.findByIsLoggedIn", query = "SELECT s FROM Sessions s WHERE s.isLoggedIn = :isLoggedIn")})
public class Sessions implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "sessionID")
    private Integer sessionID;
    @Basic(optional = false)
    @Column(name = "isLoggedIn")
    private short isLoggedIn;
    @JoinColumn(name = "userID", referencedColumnName = "userID")
    @ManyToOne(optional = false)
    private Users userID;

    public Sessions() {
    }

    public Sessions(Integer sessionID) {
        this.sessionID = sessionID;
    }

    public Sessions(Integer sessionID, short isLoggedIn) {
        this.sessionID = sessionID;
        this.isLoggedIn = isLoggedIn;
    }

    public Integer getSessionID() {
        return sessionID;
    }

    public void setSessionID(Integer sessionID) {
        this.sessionID = sessionID;
    }

    public short getIsLoggedIn() {
        return isLoggedIn;
    }

    public void setIsLoggedIn(short isLoggedIn) {
        this.isLoggedIn = isLoggedIn;
    }

    public Users getUserID() {
        return userID;
    }

    public void setUserID(Users userID) {
        this.userID = userID;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (sessionID != null ? sessionID.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Sessions)) {
            return false;
        }
        Sessions other = (Sessions) object;
        if ((this.sessionID == null && other.sessionID != null) || (this.sessionID != null && !this.sessionID.equals(other.sessionID))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.model.Sessions[ sessionID=" + sessionID + " ]";
    }
    
}
