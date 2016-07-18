package com.ibh.systems.neoscada.generator.lib;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ItemSummaryHandlerFactoryElement extends AbstractFactoryElement
{

    private final String masterId;

    private final int handlerPriority;

    private final List<String> tags = new ArrayList<> ();

    private final String prefix;

    @Override
    public Factory getFactory ()
    {
        return Factories.DEFAULT_ITEM_SUMMARY_HANDLER_FACTORY;
    }

    public ItemSummaryHandlerFactoryElement ( final String id, final String masterId, final int handlerPriority, final List<String> tags, final String prefix )
    {
        super ( id );
        this.masterId = masterId;
        this.handlerPriority = handlerPriority;
        this.tags.addAll ( tags );
        this.prefix = prefix;
    }

    public String getMasterId ()
    {
        return this.masterId;
    }

    public int getHandlerPriority ()
    {
        return this.handlerPriority;
    }

    public List<String> getTags ()
    {
        return Collections.unmodifiableList ( this.tags );
    }

    public String getPrefix ()
    {
        return this.prefix;
    }
}
