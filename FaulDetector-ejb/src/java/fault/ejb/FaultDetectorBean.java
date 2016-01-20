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
        System.out.println("[HB " + processID + "]");
        if(!processMap.containsKey(processID))
            processMap.put(processID, ProcessStatus.UNSUSPECTED);
        
        if(processMap.get(processID) != ProcessStatus.FAILED)
            aliveSet.add(processID);
    }
    
    public void failure(String processID)
    {
        System.out.println("[FAILURE " + processID + "]");
        processMap.put(processID, ProcessStatus.FAILED);
        aliveSet.remove(processID);
    }
    
    @Timeout 
    private void checkAliveProcesses()
    {
        Set<String> newFaultySet = new HashSet<>();
        System.out.println("ALIVE -> " + aliveSet.size());
        for(String pID : processMap.keySet())
        {
            if(!aliveSet.contains(pID) && processMap.get(pID) == ProcessStatus.UNSUSPECTED)
            {
                System.out.println("Il processo " + pID + " è diventato SUSPECTED!");
                processMap.put(pID, ProcessStatus.SUSPECTED);
                newFaultySet.add(pID);
            }
        }
        
        System.out.println("FAULTY -> " + newFaultySet.size());
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
    
    private enum ProcessStatus
    {
        UNSUSPECTED,
        SUSPECTED,
        FAILED;
    }
    
}

