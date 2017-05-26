package neil.demo.jeeconf2017.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * <P>A controller to handle the entry point,
 * </P>
 * <A HREF="http://localhost:8080">http://localhost:8080</A>
 * <P>(Assuming you don't change the port from default)
 * </P>
 */
@Controller
public class IndexController {

	/**
	 * <P>Map the welcome page.
	 * </P>
	 * 
	 * @return The index page
	 */
    @GetMapping({"", "index"})
    public ModelAndView index() {
            return new ModelAndView("index");
    }
}
