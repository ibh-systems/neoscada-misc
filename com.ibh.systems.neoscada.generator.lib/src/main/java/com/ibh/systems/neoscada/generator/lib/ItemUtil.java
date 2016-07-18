package com.ibh.systems.neoscada.generator.lib;

import java.util.Arrays;

public class ItemUtil
{
    public static void addExternalInputItem ( final Configuration cfg, final String name, final String connection, final String source )
    {
        cfg.addElement ( new DataItemImportFactoryElement ( name + ".V.source", connection, source ) );
        cfg.addElement ( new MasterItemFactoryElement ( name + ".V.master", name + ".V.source" ) );
        cfg.addElement ( new ManualHandlerFactoryElement ( name + ".V.master/manual", name + ".V.master" ) );
        cfg.addElement ( new ItemSummaryHandlerFactoryElement ( name + ".V.master/sum1", name + ".V.master", 100, Arrays.asList ( "error" ), "phase1" ) );
        cfg.addElement ( new ItemSummaryHandlerFactoryElement ( name + ".V.master/sum2", name + ".V.master", 5000, Arrays.asList ( "error", "alarm", "warning", "info", "manual", "blocked" ), "phase2" ) );
        cfg.addElement ( new DataItemExportFactoryElement ( name + ".V.export/da", name + ".V.master", name + ".V", "INPUT" ) );
    }

    public static void addExternalInputBooleanItem ( final Configuration cfg, final String name, final String connection, final String source )
    {
        addExternalInputBooleanItem ( cfg, name, connection, source, false );
    }

    public static void addExternalInputBooleanItem ( final Configuration cfg, final String name, final String connection, final String source, final boolean persist )
    {
        cfg.addElement ( new DataItemImportFactoryElement ( name + ".M.source", connection, source ) );
        cfg.addElement ( new MasterItemFactoryElement ( name + ".M.master", name + ".M.source" ) );
        cfg.addElement ( new ManualHandlerFactoryElement ( name + ".M.master/manual", name + ".M.master" ) );
        cfg.addElement ( new ItemSummaryHandlerFactoryElement ( name + ".M.master/sum1", name + ".M.master", 100, Arrays.asList ( "error" ), "phase1" ) );
        cfg.addElement ( new ItemSummaryHandlerFactoryElement ( name + ".M.master/sum2", name + ".M.master", 5000, Arrays.asList ( "error", "alarm", "warning", "info", "manual", "blocked" ), "phase2" ) );
        cfg.addElement ( new DataItemExportFactoryElement ( name + ".M.export/da", name + ".M.master", name + ".M", "INPUT" ) );
    }

    public static void addExternalSetpointItemWithScale ( final Configuration cfg, final String name, final String connection, final String source, final boolean readWrite, final double scale, final double offset )
    {
        addExternalSetpointItem ( cfg, name, connection, source, readWrite );
        cfg.addElement ( new ScaleHandlerFactoryElement ( name + ".V.master/scale", name + ".V.master", scale, offset ) );
    }

    public static void addExternalInputItemWithScale ( final Configuration cfg, final String name, final String connection, final String source, final double scale, final double offset )
    {
        addExternalInputItem ( cfg, name, connection, source );
        cfg.addElement ( new ScaleHandlerFactoryElement ( name + ".V.master/scale", name + ".V.master", scale, offset ) );
    }

    public static void addExternalSetpointItem ( final Configuration cfg, final String name, final String connection, final String source, final boolean readWrite )
    {
        cfg.addElement ( new DataItemImportFactoryElement ( name + ".S.source", connection, source ) );
        cfg.addElement ( new MasterItemFactoryElement ( name + ".S.master", name + ".S.source" ) );
        cfg.addElement ( new ItemSummaryHandlerFactoryElement ( name + ".S.master/sum1", name + ".S.master", 100, Arrays.asList ( "error" ), "phase1" ) );
        cfg.addElement ( new ItemSummaryHandlerFactoryElement ( name + ".S.master/sum2", name + ".S.master", 5000, Arrays.asList ( "error", "alarm", "warning", "info", "manual", "blocked" ), "phase2" ) );
        cfg.addElement ( new DataItemExportFactoryElement ( name + ".S.export/da", name + ".S.master", name + ".S", readWrite ? "INPUT,OUTPUT" : "OUTPUT" ) );
    }

    public static void addExternalSetpointItem ( final Configuration cfg, final String name, final String connection, final String source )
    {
        addExternalSetpointItem ( cfg, name, connection, source, false );
    }

    public static void addExternalBooleanSetpointItem ( final Configuration cfg, final String name, final String connection, final String source, final boolean readWrite )
    {
        cfg.addElement ( new DataItemImportFactoryElement ( name + ".C.source", connection, source ) );
        cfg.addElement ( new MasterItemFactoryElement ( name + ".C.master", name + ".C.source" ) );
        cfg.addElement ( new ItemSummaryHandlerFactoryElement ( name + ".C.master/sum1", name + ".C.master", 100, Arrays.asList ( "error" ), "phase1" ) );
        cfg.addElement ( new ItemSummaryHandlerFactoryElement ( name + ".C.master/sum2", name + ".C.master", 5000, Arrays.asList ( "error", "alarm", "warning", "info", "manual", "blocked" ), "phase2" ) );
        cfg.addElement ( new DataItemExportFactoryElement ( name + ".C.export/da", name + ".C.master", name + ".C", readWrite ? "INPUT,OUTPUT" : "OUTPUT" ) );
    }

    public static void addExternalBooleanSetpointItem ( final Configuration cfg, final String name, final String connection, final String source )
    {
        addExternalBooleanSetpointItem ( cfg, name, connection, source, false );
    }

    public static void addConstant ( final Configuration cfg, final String name, final String value )
    {
        cfg.addElement ( new ConstantDataSourceFactoryElement ( name + ".V.constant", value ) );
        cfg.addElement ( new DataItemExportFactoryElement ( name + ".V.export/da", name + ".V.constant", name + ".V", "INPUT" ) );
    }

    public static void addBooleanConstant ( final Configuration cfg, final String name, final String value )
    {
        cfg.addElement ( new ConstantDataSourceFactoryElement ( name + ".M.constant", value ) );
        cfg.addElement ( new DataItemExportFactoryElement ( name + ".M.export/da", name + ".M.constant", name + ".M", "INPUT" ) );
    }

    public static void addPersisted ( final Configuration cfg, final String name )
    {
        cfg.addElement ( new PersistentDataSourceFactoryElement ( name + ".S.persistent" ) );
        cfg.addElement ( new MasterItemFactoryElement ( name + ".S.master", name + ".S.persistent" ) );
        cfg.addElement ( new ItemSummaryHandlerFactoryElement ( name + ".S.master/sum1", name + ".S.master", 100, Arrays.asList ( "error" ), "phase1" ) );
        cfg.addElement ( new ItemSummaryHandlerFactoryElement ( name + ".S.master/sum2", name + ".S.master", 5000, Arrays.asList ( "error", "alarm", "warning", "info", "manual", "blocked" ), "phase2" ) );
        cfg.addElement ( new DataItemExportFactoryElement ( name + ".S.export/da", name + ".S.master", name + ".S", "INPUT" ) );
    }

    public static void addBooleanPersisted ( final Configuration cfg, final String name )
    {
        cfg.addElement ( new PersistentDataSourceFactoryElement ( name + ".C.persistent" ) );
        cfg.addElement ( new MasterItemFactoryElement ( name + ".C.master", name + ".C.persistent" ) );
        cfg.addElement ( new ItemSummaryHandlerFactoryElement ( name + ".C.master/sum1", name + ".C.master", 100, Arrays.asList ( "error" ), "phase1" ) );
        cfg.addElement ( new ItemSummaryHandlerFactoryElement ( name + ".C.master/sum2", name + ".C.master", 5000, Arrays.asList ( "error", "alarm", "warning", "info", "manual", "blocked" ), "phase2" ) );
        cfg.addElement ( new DataItemExportFactoryElement ( name + ".C.export/da", name + ".C.master", name + ".C", "INPUT" ) );
    }
}
