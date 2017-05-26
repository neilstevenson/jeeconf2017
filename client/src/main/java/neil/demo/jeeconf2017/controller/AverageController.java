package neil.demo.jeeconf2017.controller;

import java.time.Duration;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.hazelcast.jet.DAG;
import com.hazelcast.jet.JetInstance;
import com.hazelcast.jet.Job;

import lombok.extern.slf4j.Slf4j;
import neil.demo.jeeconf2017.Constants;
import neil.demo.jeeconf2017.domain.CurrencyAverage;
import neil.demo.jeeconf2017.jet.MaDAG;
import neil.demo.jeeconf2017.util.AverageFormatter;
import neil.demo.jeeconf2017.util.ReflectionUtil;

/**
 * <P>A controller to handle the averages operations:
 * </P>
 * <OL>
 * <LI><P>{@code exponential} - Return the calculated exponential moving averages
 * to the screen.
 * <LI><P>{@code simple} - Return the calculated simple moving averages
 * to the screen.
 * </P></LI>
 * <LI><P>{@code start} - Run and time the execution of the Moving Averages DAG.
 * </P></LI>
 */
@Controller
@RequestMapping("average")
@Slf4j
public class AverageController {

	@Autowired
	private AverageFormatter averageFormatter;
	@Autowired
	private JetInstance jetInstance;

    /**
     * <P>Retrieve the exponential moving averages.
     * </P>
     * 
     * @return A page with model attributes to show.
     */
    @GetMapping("exponential")
    public ModelAndView exponential() {
            
            ModelAndView modelAndView = 
                            new ModelAndView("average/exponential");

            modelAndView.addObject("columns", ReflectionUtil.getColumns(CurrencyAverage.class));
            modelAndView.addObject("data" 
                    ,ReflectionUtil.getData(this.averageFormatter.read(Constants.MAP_EXPONENTIAL_MOVING_AVERAGE)
    												,CurrencyAverage.class));
            
            return modelAndView;
    }

    

	/**
     * <P>Retrieve the simple moving averages.
     * </P>
     * 
     * @return A page with model attributes to show.
     */
    @GetMapping("simple")
    public ModelAndView simple() {
            
            ModelAndView modelAndView = 
                            new ModelAndView("average/simple");

            modelAndView.addObject("columns", ReflectionUtil.getColumns(CurrencyAverage.class));
            modelAndView.addObject("data" 
                            ,ReflectionUtil.getData(this.averageFormatter.read(Constants.MAP_SIMPLE_MOVING_AVERAGE)
                            						,CurrencyAverage.class));
            
            return modelAndView;
    }

    /**
     * <P>Initiate the Jet job and wait for the answer.
     * Treat zero as a fault.
     * </P>
     * 
     * @param j_last The last 10 results, or some other number
     * @return Render same page, enriched
     */
    @GetMapping("start")
    public ModelAndView start(@RequestParam(name="j_last", required=false) String j_last) {
            
            ModelAndView modelAndView = 
                            new ModelAndView("average/start");

    		if (j_last==null) {
    			// First page render, show "Load" button but do not processing
    		} else {
    			// Second page render, assume "Load" button pressed so do processing 
    			
    			Instant start = Instant.now();

                try {
                    int count = Integer.valueOf(j_last.trim());

                    DAG dag = new MaDAG(count);
                    
                    Job job = this.jetInstance.newJob(dag);
                    
                    job.execute().get();
                    
        			Duration elapsed = Duration.between(start, Instant.now());
                    
                    // Zero means failed
                    if (!elapsed.equals(Duration.ZERO)) {
                            modelAndView.addObject("j_elapsed", elapsed.toString());
                    }
                    modelAndView.addObject("j_last", j_last);

                } catch (Exception e) {
                    log.error("start", e);
                }
    			
    		}

            return modelAndView;
    }
	
}
