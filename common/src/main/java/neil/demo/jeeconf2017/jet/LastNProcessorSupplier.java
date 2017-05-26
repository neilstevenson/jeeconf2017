package neil.demo.jeeconf2017.jet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.hazelcast.jet.ProcessorSupplier;

/**
 * <P>Create multiple {@link LastNProcessor} instances on demand,
 * using the provided parameter in the constructor for all of
 * them. Ie. return some identically configured clones.
 * </P>
 */
@SuppressWarnings("serial")
public class LastNProcessorSupplier implements ProcessorSupplier {

	private final int last;
	
	public LastNProcessorSupplier(final int arg0) {
		this.last = arg0;
	}

	/**
	 * <P>Create a number of processing instances, each
	 * configured with the same parameter provided to
	 * the constructor.
	 * </P>
	 * 
	 * @param requiredNumber How many to create
	 * @return A collection of the required size
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Collection get(int requiredNumber) {
		List<LastNProcessor> result = new ArrayList<>();
		for (int i=0; i<requiredNumber; i++) {
			result.add(new LastNProcessor(this.last));
		}
		return result;
	}

}
