package de.imi.odmtoolbox.convert.converter;

import de.imi.odmtoolbox.convert.enums.ConvertMethodEnumMarker;
import de.imi.odmtoolbox.convert.helper.lambda.LambdaHelper;
import de.imi.odmtoolbox.convert.websocket.PushMessageService;
import de.imi.odmtoolbox.library.ConversionNotes;
import lombok.Getter;
import lombok.Setter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractConverter {

    @Setter
    @Getter
    public static class RedcapFileWrapper {
        Document redcapFile;
    }

    protected final double SYSTEM_FACTOR = 1.0;

    protected Map<ConvertMethodEnumMarker, String> converterToMethodNameMap;
    protected Map<ConvertMethodEnumMarker, Method> methodMap;
    protected Document importedODM;
    protected ConversionNotes conversionNotes;
    protected PushMessageService messageService;

    protected AbstractConverter(Document importedODM, ConversionNotes conversionNote, PushMessageService messageService) {
        this.importedODM = importedODM;
        this.conversionNotes = conversionNote;
        this.messageService = messageService;
        converterToMethodNameMap = this.createConverterMethodToMethodNameMap();
        methodMap = this.createMethodMap();
    }

    protected abstract Map<ConvertMethodEnumMarker, String> createConverterMethodToMethodNameMap();
    protected Map<ConvertMethodEnumMarker, Method> createMethodMap() {
        Map<ConvertMethodEnumMarker, Method> map = new HashMap<>();
        Class<?> clazz = this.getClass();
        this.converterToMethodNameMap.forEach(LambdaHelper.throwingBiConsumerWrapper((k, v)->map.put(k, clazz.getDeclaredMethod(v))));
        return map;
    }

    public abstract void convert();

    protected void executeMethod(ConvertMethodEnumMarker option) throws InvocationTargetException, IllegalAccessException {
        Method method = getExecutingMethod(option);
        method.setAccessible(true);
        method.invoke(this);
    }

    private Method getExecutingMethod(ConvertMethodEnumMarker option) {
        return methodMap.get(option);
    }

    protected String getElementIdentifierString(Element element, String identifier) {
        return element.getNodeName() + "(" + element.getAttribute(identifier) + ")";
    }
}

