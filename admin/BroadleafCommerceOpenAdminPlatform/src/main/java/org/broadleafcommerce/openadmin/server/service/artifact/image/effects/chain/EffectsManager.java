package org.broadleafcommerce.openadmin.server.service.artifact.image.effects.chain;

import org.broadleafcommerce.openadmin.server.service.artifact.OperationBuilder;
import org.broadleafcommerce.openadmin.server.service.artifact.image.Operation;
import org.broadleafcommerce.openadmin.server.service.artifact.image.effects.chain.conversion.ConversionManager;
import org.broadleafcommerce.openadmin.server.service.artifact.image.effects.chain.conversion.Parameter;
import org.broadleafcommerce.openadmin.server.service.artifact.image.effects.chain.filter.*;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("blImageEffectsManager")
public class EffectsManager {

    @Resource(name="blImageConversionManager")
	protected ConversionManager conversionManager;
	protected Map<String, OperationBuilder> filters = new HashMap<String, OperationBuilder>();

    public EffectsManager() {
        filters.put(FilterTypeEnum.ALTERHSB.toString(), new AlterHSB());
        filters.put(FilterTypeEnum.ALTERRGB.toString(), new AlterRGB());
        filters.put(FilterTypeEnum.AUTOLEVELSRGB.toString(), new AutoLevelsRGB());
        filters.put(FilterTypeEnum.CROP.toString(), new Crop());
        filters.put(FilterTypeEnum.GAUSSIANBLUR.toString(), new GaussianBlur());
        filters.put(FilterTypeEnum.RESIZE.toString(), new Resize());
        filters.put(FilterTypeEnum.ROTATE.toString(), new Rotate());
        filters.put(FilterTypeEnum.UNSHARPMASK.toString(), new UnsharpMask());
    }

    public Operation[] buildOperations(Map<String, String[]> parameterMap, InputStream artifactStream, String mimeType) {
        List<Operation> operations = new ArrayList<Operation>();
        for (OperationBuilder builder : filters.values()) {
            Operation operation = builder.buildOperation(parameterMap, artifactStream, mimeType);
            if (operation != null) {
                operations.add(operation);
            }
        }

        return operations.toArray(new Operation[]{});
    }

	public BufferedImage renderEffect(String effectName, Double factor, UnmarshalledParameter[] parameters, BufferedImage src) throws Exception {
		/*
		 * retrieve the injected filter, instantiate the filter instance using reflection and execute the operation
		 */
		Object filterObject = filters.get(effectName);
		if (filterObject == null) {
			throw new FilterNotFoundException("An effects filter was not found for the name: " + effectName);
		}
        Class filterClass = filterObject.getClass();
		
		Parameter[] marshalledParameters = new Parameter[parameters.length];
		for (int j=0;j<parameters.length;j++) {
			marshalledParameters[j] = conversionManager.convertParameter(parameters[j].getValue(), parameters[j].getType(), factor, parameters[j].isApplyFactor());
		}
		
		Class[] types = new Class[marshalledParameters.length + 1];
		Object[] args = new Object[marshalledParameters.length + 1];
		for (int j=0;j<types.length-1;j++){
			types[j] = marshalledParameters[j].getParameterClass();
			args[j] = marshalledParameters[j].getParameterInstance();
		}
		types[types.length-1] = RenderingHints.class;
		args[types.length-1] = null;
		Constructor constructor = filterClass.getConstructor(types);
		Object filterInstance = constructor.newInstance(args);
		
		Method filterMethod = filterClass.getMethod("filter", new Class[]{BufferedImage.class, BufferedImage.class});
		Object result = filterMethod.invoke(filterInstance, new Object[]{src, null});
		
		return (BufferedImage) result;
	}

	/**
	 * @return the filters
	 */
	public Map<String, OperationBuilder> getFilters() {
		return filters;
	}

	/**
	 * @param filters the filters to set
	 */
	public void setFilters(Map<String, OperationBuilder> filters) {
		this.filters = filters;
	}

	/**
	 * @return the parameterConverter
	 */
	public ConversionManager getConversionManager() {
		return conversionManager;
	}

	/**
	 * @param parameterConverter the parameterConverter to set
	 */
	public void setConversionManager(ConversionManager conversionManager) {
		this.conversionManager = conversionManager;
	}
}
