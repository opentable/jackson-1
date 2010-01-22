package org.codehaus.jackson.map.jsontype.impl;

import java.io.IOException;

import org.codehaus.jackson.*;
import org.codehaus.jackson.map.*;
import org.codehaus.jackson.map.annotate.JsonTypeInfo;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.util.JsonParserSequence;
import org.codehaus.jackson.util.TokenBuffer;

/**
 * Type deserializer used with {@link JsonTypeInfo.As#PROPERTY}
 * inclusion mechanism.
 * Uses regular form (additional key/value entry before actual data)
 * when typed object is expressed as JSON Object; otherwise behaves similar to how
 * {@link JsonTypeInfo.As#WRAPPER_ARRAY} works.
 * Latter is used if JSON representation is polymorphic
 * 
 * @since 1.5
 * @author tatu
 */
public class AsPropertyTypeDeserializer extends AsArrayTypeDeserializer
{
    protected final String _propertyName;

    public AsPropertyTypeDeserializer(JavaType bt, TypeConverter conv, String propName) {
        super(bt, conv);
        _propertyName = propName;
    }

    @Override
    public JsonTypeInfo.As getTypeInclusion() {
        return JsonTypeInfo.As.PROPERTY;
    }

    /**
     * This is the trickiest thing to handle, since property we are looking
     * for may be anywhere...
     */
    @Override
    public Object deserializeTypedFromObject(JsonParser jp, DeserializationContext ctxt)
        throws IOException, JsonProcessingException
    {
        // but first, sanity check to ensure we have START_OBJECT...
        if (jp.getCurrentToken() != JsonToken.START_OBJECT) {
            throw ctxt.wrongTokenException(jp, JsonToken.START_OBJECT,
                    "need JSON Object to contain As.PROPERTY type information (for class "+baseTypeName()+")");
        }
        // Ok, let's try to find the property. But first, need token buffer...
        TokenBuffer tb = new TokenBuffer(null);
        tb.writeStartObject();
        while (jp.nextValue() != JsonToken.END_OBJECT) {
            if (_propertyName.equals(jp.getCurrentName())) { // gotcha!
                JsonDeserializer<Object> deser = _findDeserializer(ctxt, jp.getText());
                jp = JsonParserSequence.createFlattened(tb.asParser(jp), jp);
                jp.nextToken(); // will be START_OBJECT from TokenBuffer
                // deserializer should take care of closing END_OBJECT as well
                return deser.deserialize(jp, ctxt);
            }
            tb.copyCurrentStructure(jp);
        }
        // Error if we get here...
        throw ctxt.wrongTokenException(jp, JsonToken.FIELD_NAME,
                "missing property '"+_propertyName+"' that is to contain type id  (for class "+baseTypeName()+")");
    }

    // These are fine from base class:
    //public Object deserializeTypedArray(JsonParser jp, DeserializationContext ctxt)
    //public Object deserializeTypedScalar(JsonParser jp, DeserializationContext ctxt)    
    //public Object deserializeTypedUnknown(JsonParser jp, DeserializationContext ctxt)    
}
