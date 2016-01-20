package fault.ejb;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.LocalBean;
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
                                                      2 * 1000, 
                                                      new TimerConfig());
    }
    
    public void keepAlive(String processID)
    {
        processMap.put(processID, ProcessStatus.UNSUSPECTED);
        aliveSet.add(processID);
    }
    
    @Timeout 
    private void checkAliveProcesses()
    {
        /*Set<String> newFaultySet = getFaultySet();
        
        for(String pID : newFaultySet)
        {
            ProcessStatus status = processMap.get(pID);
            
            switch  (status)
            {
                case UNSUSPECTED:
                    System.out.println("Il Processo " + pID + " è diventato SUSPECTED");
                    processMap.put(pID, ProcessStatus.SUSPECTED);
                    break;
                case SUSPECTED:
                    break;
                case FAILED:
                    break;
            }   
        }
        
        checkRevenantProcesses(newFaultySet);
        
        this.faultySet = newFaultySet;
        this.aliveSet.clear();*/
        
        Set<String> newFaultySet = new HashSet<>();
        
        for(String pID : processMap.keySet())
        {
            if(!aliveSet.contains(pID))
            {
                System.out.println("Il processo " + pID + " è diventato SUSPECTED!");
                processMap.put(pID, ProcessStatus.SUSPECTED);
                newFaultySet.add(pID);
            }
        }
        
        for(String pID : faultySet)
        {
            if(aliveSet.contains(pID) && processMap.get(pID) != ProcessStatus.FAILED)
            {
                System.out.println("Il processo " + pID + " è tornato UNSUSPECTED!");
                processMap.put(pID, ProcessStatus.UNSUSPECTED);
            }
        }
        
        this.faultySet = newFaultySet;
        this.aliveSet.clear();
    }
    
    void checkRevenantProcesses(Set<String> newFaultySet)
    {
        Set<String> revenantSet = new HashSet<>(this.faultySet);
        System.out.println("Dimensione Faulty set : " + faultySet.size());
        System.out.println("Dimensione NEW Faulty set : " + newFaultySet.size());
        if (revenantSet.removeAll(newFaultySet))
        {
            for (String revenantID : revenantSet)
            {
                if (processMap.get(revenantID) != ProcessStatus.FAILED)
                {
                    System.out.println("Il Processo " + revenantID + " è tornato UNSUSPECTED");
                    processMap.put(revenantID, ProcessStatus.UNSUSPECTED);
                }
            }
        }
    }

    private Set<String> getFaultySet()
    {
        Set<String> faultySet = new HashSet<>(processMap.keySet());
        faultySet.removeAll(aliveSet);
        System.out.println("I Faulty Process sono: " + faultySet.size());
        return faultySet;
    }
    
    private enum ProcessStatus
    {
        UNSUSPECTED,
        SUSPECTED,
        FAILED;
    }
    
}

