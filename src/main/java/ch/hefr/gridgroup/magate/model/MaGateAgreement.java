package ch.hefr.gridgroup.magate.model;

import java.util.UUID;

import org.apache.log4j.Logger;
import org.ogf.graap.wsag.api.Agreement;
import org.ogf.graap.wsag.api.exceptions.ResourceUnavailableException;
import org.ogf.graap.wsag.api.exceptions.ResourceUnknownException;
import org.ogf.graap.wsag.api.types.WSAGXmlType;
import org.ogf.schemas.graap.wsAgreement.AgreementContextType;
import org.ogf.schemas.graap.wsAgreement.AgreementPropertiesType;
import org.ogf.schemas.graap.wsAgreement.AgreementRoleType;
import org.ogf.schemas.graap.wsAgreement.AgreementStateDefinition;
import org.ogf.schemas.graap.wsAgreement.AgreementStateDocument;
import org.ogf.schemas.graap.wsAgreement.AgreementStateType;
import org.ogf.schemas.graap.wsAgreement.GuaranteeTermStateDocument;
import org.ogf.schemas.graap.wsAgreement.GuaranteeTermStateType;
import org.ogf.schemas.graap.wsAgreement.ServiceTermStateDocument;
import org.ogf.schemas.graap.wsAgreement.ServiceTermStateType;
import org.ogf.schemas.graap.wsAgreement.TermTreeType;
import org.ogf.schemas.graap.wsAgreement.TerminateInputType;
import org.ogf.schemas.graap.wsAgreement.AgreementStateDefinition.Enum;
import org.w3c.dom.Text;

/**
 * 
 * @author yehuang
 */
public class MaGateAgreement extends WSAGXmlType implements Agreement {

	private static final Logger log = Logger.getLogger(MaGateAgreement.class);
	
	private AgreementPropertiesType agreementProperties  = AgreementPropertiesType.Factory.newInstance();
	
	public MaGateAgreement() {
		super();
        initialize();
	}
	
	@Override
	protected void initialize() {
		
		agreementProperties.setAgreementId("AgreementID_" + UUID.randomUUID().toString());
        agreementProperties.addNewContext().setServiceProvider(AgreementRoleType.AGREEMENT_RESPONDER);
        agreementProperties.addNewAgreementState().setState(AgreementStateDefinition.PENDING);

        TermTreeType terms = agreementProperties.addNewTerms();

        // will be fixed in MUSE version 2.2.0
        // Muse Hack Issue 159 - Muse does not honor empty elements and therefore fails 
        // if an element contaiins no children and the children are optional (minOccours = 0)
        // therefore we simply include a white space as text content
        // should be removed as soon as fixed

//        Text text = terms.getDomNode().getOwnerDocument().createTextNode(" ");
//        terms.getDomNode().appendChild(text);
	}

	/**
	 * Getter
	 */

	public String getAgreementId() throws ResourceUnknownException,
			ResourceUnavailableException {
		// TODO Auto-generated method stub
		return agreementProperties.getAgreementId();
	}

	public AgreementContextType getContext() throws ResourceUnknownException,
			ResourceUnavailableException {
		// TODO Auto-generated method stub
		return agreementProperties.getContext();
	}
	
	public String getName() throws ResourceUnknownException,
		ResourceUnavailableException {
		// TODO Auto-generated method stub
		return agreementProperties.getName();
	}
	
	public TermTreeType getTerms() throws ResourceUnknownException,
		ResourceUnavailableException {
		// TODO Auto-generated method stub
		return agreementProperties.getTerms();
	}

	public GuaranteeTermStateDocument[] getGuaranteeTermStates()
			throws ResourceUnknownException, ResourceUnavailableException {
		// TODO Auto-generated method stub
		
		GuaranteeTermStateType[] guarateeStates = agreementProperties.getGuaranteeTermStateArray();
        
        if (guarateeStates == null) { 
            return new GuaranteeTermStateDocument[0];
        }
        
        GuaranteeTermStateDocument[] result = new GuaranteeTermStateDocument[guarateeStates.length];
        for (int i = 0; i < guarateeStates.length; i++) {
            result[i] = GuaranteeTermStateDocument.Factory.newInstance();
            result[i].addNewGuaranteeTermState().set(guarateeStates[i]);
        }
        
        return result; 
	}

	public ServiceTermStateDocument[] getServiceTermStates()
			throws ResourceUnknownException, ResourceUnavailableException {
		// TODO Auto-generated method stub
		
		ServiceTermStateType[] serviceStates = agreementProperties.getServiceTermStateArray();
        
        if (serviceStates == null) { 
            return new ServiceTermStateDocument[0];
        }

        ServiceTermStateDocument[] result = new ServiceTermStateDocument[serviceStates.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = ServiceTermStateDocument.Factory.newInstance();
            result[i].addNewServiceTermState().set(serviceStates[i]);
        }
        
        return result;
	}

	public AgreementStateDocument getState() throws ResourceUnknownException,
			ResourceUnavailableException {
		// TODO Auto-generated method stub
		
		AgreementStateDocument document = AgreementStateDocument.Factory.newInstance();
        document.addNewAgreementState().set(agreementProperties.getAgreementState());
        return document;
	}
	
	public AgreementStateDefinition.Enum getStateEnum() throws ResourceUnknownException,
		ResourceUnavailableException {
		// TODO Auto-generated method stub
		
		return agreementProperties.getAgreementState().getState();
		
	}

	/**
	 * Setter
	 */

	public void setAgreementId(String agreementId) {
        agreementProperties.setAgreementId(agreementId);
    }

    public void setContext(AgreementContextType context) {
        agreementProperties.setContext(context);
    }

    public void setName(String name) {
        agreementProperties.setName(name);
    }

    public void setTerms(TermTreeType terms) {
        agreementProperties.setTerms(terms);
    }
    
	public void setState(AgreementStateType agreementState) {
        agreementProperties.setAgreementState(agreementState);
    }
	
	public void setStateEnum(AgreementStateDefinition.Enum agreementStateDefinition) {
		
		AgreementStateDocument agreementStateDoc = AgreementStateDocument.Factory.newInstance();
        AgreementStateType agreementState        = agreementStateDoc.addNewAgreementState();

        agreementState.setState(agreementStateDefinition);
        
        agreementProperties.setAgreementState(agreementState);
    }

    public void setGuaranteeTermStates(GuaranteeTermStateType[] guaranteeTermStateList) {
        agreementProperties.setGuaranteeTermStateArray(guaranteeTermStateList);
    }

    public void setServiceTermStateList(ServiceTermStateType[] serviceTermStateList) {
        agreementProperties.setServiceTermStateArray(serviceTermStateList);
    }

	public void terminate(TerminateInputType reason)
			throws ResourceUnknownException, ResourceUnavailableException {
		// TODO Auto-generated method stub

	}
	
	/**
	 * methods from WSAGXmlType
	 */

	@Override
	public boolean validate() {
		// TODO Auto-generated method stub
		return validate(agreementProperties);
	}
	
}
