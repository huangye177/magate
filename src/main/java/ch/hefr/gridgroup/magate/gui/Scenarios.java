/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.hefr.gridgroup.magate.gui;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 *
 * @author yehuang
 */
@Entity
@Table(name = "SCENARIOS", catalog = "MAGATE", schema = "PUBLIC")
@NamedQueries({@NamedQuery(name = "Scenarios.findAll", query = "SELECT s FROM Scenarios s"), @NamedQuery(name = "Scenarios.findById", query = "SELECT s FROM Scenarios s WHERE s.id = :id"), @NamedQuery(name = "Scenarios.findByScenarioId", query = "SELECT s FROM Scenarios s WHERE s.scenarioId = :scenarioId"), @NamedQuery(name = "Scenarios.findByNumberOfMagate", query = "SELECT s FROM Scenarios s WHERE s.numberOfMagate = :numberOfMagate"), @NamedQuery(name = "Scenarios.findByAllowCommunityExecution", query = "SELECT s FROM Scenarios s WHERE s.allowCommunityExecution = :allowCommunityExecution"), @NamedQuery(name = "Scenarios.findByResDiscoveryProtocol", query = "SELECT s FROM Scenarios s WHERE s.resDiscoveryProtocol = :resDiscoveryProtocol"), @NamedQuery(name = "Scenarios.findByDelegationQueueLimit", query = "SELECT s FROM Scenarios s WHERE s.delegationQueueLimit = :delegationQueueLimit"), @NamedQuery(name = "Scenarios.findByTimeSearchCommunity", query = "SELECT s FROM Scenarios s WHERE s.timeSearchCommunity = :timeSearchCommunity"), @NamedQuery(name = "Scenarios.findByAllowMultiNegotiation", query = "SELECT s FROM Scenarios s WHERE s.allowMultiNegotiation = :allowMultiNegotiation"), @NamedQuery(name = "Scenarios.findByNegotiationLimit", query = "SELECT s FROM Scenarios s WHERE s.negotiationLimit = :negotiationLimit"), @NamedQuery(name = "Scenarios.findByInteractionApproach", query = "SELECT s FROM Scenarios s WHERE s.interactionApproach = :interactionApproach"), @NamedQuery(name = "Scenarios.findByActivated", query = "SELECT s FROM Scenarios s WHERE s.activated = :activated"), @NamedQuery(name = "Scenarios.findBySequence", query = "SELECT s FROM Scenarios s WHERE s.sequence = :sequence")})
public class Scenarios implements Serializable {
    @Transient
    private PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "ID")
    private Integer id;
    @Column(name = "SCENARIO_ID")
    private String scenarioId;
    @Column(name = "NUMBER_OF_MAGATE")
    private Integer numberOfMagate;
    @Column(name = "ALLOW_COMMUNITY_EXECUTION")
    private Integer allowCommunityExecution = 1;
    @Column(name = "RES_DISCOVERY_PROTOCOL")
    private String resDiscoveryProtocol;
    @Column(name = "DELEGATION_QUEUE_LIMIT")
    private Integer delegationQueueLimit;
    @Column(name = "TIME_SEARCH_COMMUNITY")
    private Integer timeSearchCommunity;
    @Column(name = "ALLOW_MULTI_NEGOTIATION")
    private Integer allowMultiNegotiation = 0;
    @Column(name = "NEGOTIATION_LIMIT")
    private Integer negotiationLimit;
    @Column(name = "INTERACTION_APPROACH")
    private String interactionApproach;
    @Column(name = "ACTIVATED")
    private Integer activated = 1;
    @Column(name = "SEQUENCE")
    private Integer sequence;

    public Scenarios() {
    }

    public Scenarios(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        Integer oldId = this.id;
        this.id = id;
        changeSupport.firePropertyChange("id", oldId, id);
    }

    public String getScenarioId() {
        return scenarioId;
    }

    public void setScenarioId(String scenarioId) {
        String oldScenarioId = this.scenarioId;
        this.scenarioId = scenarioId;
        changeSupport.firePropertyChange("scenarioId", oldScenarioId, scenarioId);
    }

    public Integer getNumberOfMagate() {
        return numberOfMagate;
    }

    public void setNumberOfMagate(Integer numberOfMagate) {
        Integer oldNumberOfMagate = this.numberOfMagate;
        this.numberOfMagate = numberOfMagate;
        changeSupport.firePropertyChange("numberOfMagate", oldNumberOfMagate, numberOfMagate);
    }

    public Integer getAllowCommunityExecution() {
        return allowCommunityExecution;
    }

    public void setAllowCommunityExecution(Integer allowCommunityExecution) {
        Integer oldAllowCommunityExecution = this.allowCommunityExecution;
        this.allowCommunityExecution = allowCommunityExecution;
        changeSupport.firePropertyChange("allowCommunityExecution", oldAllowCommunityExecution, allowCommunityExecution);
    }

    public String getResDiscoveryProtocol() {
        return resDiscoveryProtocol;
    }

    public void setResDiscoveryProtocol(String resDiscoveryProtocol) {
        String oldResDiscoveryProtocol = this.resDiscoveryProtocol;
        this.resDiscoveryProtocol = resDiscoveryProtocol;
        changeSupport.firePropertyChange("resDiscoveryProtocol", oldResDiscoveryProtocol, resDiscoveryProtocol);
    }

    public Integer getDelegationQueueLimit() {
        return delegationQueueLimit;
    }

    public void setDelegationQueueLimit(Integer delegationQueueLimit) {
        Integer oldDelegationQueueLimit = this.delegationQueueLimit;
        this.delegationQueueLimit = delegationQueueLimit;
        changeSupport.firePropertyChange("delegationQueueLimit", oldDelegationQueueLimit, delegationQueueLimit);
    }

    public Integer getTimeSearchCommunity() {
        return timeSearchCommunity;
    }

    public void setTimeSearchCommunity(Integer timeSearchCommunity) {
        Integer oldTimeSearchCommunity = this.timeSearchCommunity;
        this.timeSearchCommunity = timeSearchCommunity;
        changeSupport.firePropertyChange("timeSearchCommunity", oldTimeSearchCommunity, timeSearchCommunity);
    }

    public Integer getAllowMultiNegotiation() {
        return allowMultiNegotiation;
    }

    public void setAllowMultiNegotiation(Integer allowMultiNegotiation) {
        Integer oldAllowMultiNegotiation = this.allowMultiNegotiation;
        this.allowMultiNegotiation = allowMultiNegotiation;
        changeSupport.firePropertyChange("allowMultiNegotiation", oldAllowMultiNegotiation, allowMultiNegotiation);
    }

    public Integer getNegotiationLimit() {
        return negotiationLimit;
    }

    public void setNegotiationLimit(Integer negotiationLimit) {
        Integer oldNegotiationLimit = this.negotiationLimit;
        this.negotiationLimit = negotiationLimit;
        changeSupport.firePropertyChange("negotiationLimit", oldNegotiationLimit, negotiationLimit);
    }

    public String getInteractionApproach() {
        return interactionApproach;
    }

    public void setInteractionApproach(String interactionApproach) {
        String oldInteractionApproach = this.interactionApproach;
        this.interactionApproach = interactionApproach;
        changeSupport.firePropertyChange("interactionApproach", oldInteractionApproach, interactionApproach);
    }

    public Integer getActivated() {
        return activated;
    }

    public void setActivated(Integer activated) {
        Integer oldActivated = this.activated;
        this.activated = activated;
        changeSupport.firePropertyChange("activated", oldActivated, activated);
    }

    public Integer getSequence() {
        return sequence;
    }

    public void setSequence(Integer sequence) {
        Integer oldSequence = this.sequence;
        this.sequence = sequence;
        changeSupport.firePropertyChange("sequence", oldSequence, sequence);
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Scenarios)) {
            return false;
        }
        Scenarios other = (Scenarios) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ch.hefr.gridgroup.magate.Scenarios[id=" + id + "]";
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(listener);
    }

}
