package fault.ws;

import fault.ejb.FaultDetectorBean;
import javax.ejb.EJB;
import javax.jws.Oneway;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

/**
 *
 * @author Damiano Di Stefano, Marco Giuseppe Salafia
 */
@WebService(serviceName = "NewWebService")
public class NewWebService
{

    @EJB
    private FaultDetectorBean ejbRef;// Add business logic below. (Right-click in editor and choose
    // "Insert Code > Add Web Service Operation")

    @WebMethod(operationName = "keepAlive")
    @Oneway
    public void keepAlive(@WebParam(name = "processID") String processID)
    {
        ejbRef.keepAlive(processID);
    }
    
}