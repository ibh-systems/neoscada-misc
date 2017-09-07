package org.eclipse.neoscada.contrib.kafka;

import java.lang.reflect.Type;

import org.eclipse.scada.core.NotConvertableException;
import org.eclipse.scada.core.NullValueException;
import org.eclipse.scada.core.Variant;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class VariantSerializer implements JsonSerializer<Variant>
{
    @Override
    public JsonElement serialize ( Variant src, Type typeOfSrc, JsonSerializationContext context )
    {
        try
        {
            switch ( src.getType () )
            {
                case NULL:
                    return JsonNull.INSTANCE;
                case BOOLEAN:
                    return new JsonPrimitive ( src.asBoolean () );
                case INT32:
                    return new JsonPrimitive ( src.asInteger () );
                case INT64:
                    return new JsonPrimitive ( src.asLong () );
                case DOUBLE:
                    return new JsonPrimitive ( src.asDouble () );
                case STRING:
                    return new JsonPrimitive ( src.asString () );
                default:
                    return new JsonPrimitive ( src.asString ( "ERROR: not convertable" ) );
            }
        }
        catch ( NullValueException e )
        {
            return new JsonPrimitive ( e.getMessage () );
        }
        catch ( NotConvertableException e )
        {
            return new JsonPrimitive ( e.getMessage () );
        }
    }
}
