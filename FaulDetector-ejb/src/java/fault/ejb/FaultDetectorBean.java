package fault.ejb;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Singleton;
import javax.ejb.LocalBean;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;

/**
 *
 * @author Damiano Di Stefano, Marco Giuseppe Salafia
 */
@Singleton
@LocalBean
@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
public class FaultDetectorBean
{
    @Resource
    TimerService timerService;
    
    HashMap<String, ProcessStatus> processMap;
    Set<String> aliveSet;
    Set<String> faultySet;
    
    Timer timer;
    
    @PostConstruct
    private void init()
    {
        this.processMap = new HashMap<>();
        this.aliveSet = new HashSet<>();
        this.faultySet = new HashSet<>();
        this.timer = timerService.createIntervalTimer(10, 
                                                      5 * 1000, 
                                                      new TimerConfig());
    }
    
    @Lock(LockType.WRITE)
    public void keepAlive(String processID)
    {
        System.out.println("[HB " + processID + "]");
        //Se non contiene il processo lo registra
        if(!processMap.containsKey(processID))
            processMap.put(processID, ProcessStatus.UNSUSPECTED);
        
        //Se il processo non è FAILED lo registra come Vivo
        if(processMap.get(processID) != ProcessStatus.FAILED)
            aliveSet.add(processID);
    }
    
    @Lock(LockType.WRITE)
    public void failure(String processID)
    {
        System.out.println("[FAILURE " + processID + "]");
        //Setta il processo come FAILED
        processMap.put(processID, ProcessStatus.FAILED);
        //Lo rimuove eventualmente dai vivi se lo ha settato precedentemente
        aliveSet.remove(processID);
    }
    
    @Timeout 
    @Lock(LockType.WRITE)
    private void checkAliveProcesses()
    {
        System.out.println("ALIVE -> " + aliveSet.size());
        for(String pID : processMap.keySet())
        {
            if(!aliveSet.contains(pID) && processMap.get(pID) == ProcessStatus.UNSUSPECTED)
            {
                System.out.println("Il processo " + pID + " è diventato SUSPECTED!");
                processMap.put(pID, ProcessStatus.SUSPECTED);
                faultySet.add(pID);
            }
        }
        
        System.out.println("FAULTY -> " + faultySet.size());
        
        //Per ogni processo nell'insieme Faulty
        HashSet<String> faultyCopy = new HashSet<>(faultySet);
        for(String pID : faultyCopy)
        {
            //se è vivo a questo giro vuol dire che non è più SUSPECTED
            if(aliveSet.contains(pID))
            {
                System.out.println("Il processo " + pID + " è tornato UNSUSPECTED!");
                processMap.put(pID, ProcessStatus.UNSUSPECTED);
                faultySet.remove(pID);
            }
        }
        
        //resetto i vivi di questo Round
        this.aliveSet.clear();
    }
    
    private enum ProcessStatus
    {
        UNSUSPECTED,
        SUSPECTED,
        FAILED;
    }
    
}

