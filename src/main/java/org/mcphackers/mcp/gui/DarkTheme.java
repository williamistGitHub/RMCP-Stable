package org.mcphackers.mcp.gui;

import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.metal.OceanTheme;
import java.awt.*;
import java.util.Arrays;
import java.util.List;

public class DarkTheme extends OceanTheme {

    @Override
    protected ColorUIResource getBlack() {
        return new ColorUIResource(0xffffff);
    }

    @Override
    protected ColorUIResource getWhite() {
        return new ColorUIResource(0x000000);
    }

    @Override
    public ColorUIResource getControlTextColor() {
        return getBlack();
    }

    @Override
    public ColorUIResource getDesktopColor() {
        return getWhite();
    }

    @Override
    public ColorUIResource getInactiveControlTextColor() {
        return new ColorUIResource(0x333333);
    }

    @Override
    public ColorUIResource getMenuDisabledForeground() {
        return getInactiveControlTextColor();
    }

    @Override
    public ColorUIResource getFocusColor() {
        return getBlack();
    }

    @Override
    public ColorUIResource getMenuSelectedForeground() {
        return getWhite();
    }

    @Override
    public ColorUIResource getControl() {
        return getWhite();
    }

    @Override
    public ColorUIResource getMenuBackground() {
        return getWhite();
    }

    @Override
    public ColorUIResource getControlShadow() {
        return getWhite();
    }

    @Override
    public ColorUIResource getControlDarkShadow() {
        return getFocusColor();
    }

    @Override
    public ColorUIResource getMenuSelectedBackground() {
        return getFocusColor();
    }

    @Override
    public ColorUIResource getControlHighlight() {
        return getWhite();
    }

    @Override
    protected ColorUIResource getPrimary1() {
        return getFocusColor();
    }

    @Override
    protected ColorUIResource getPrimary2() {
        return getFocusColor();
    }

    @Override
    protected ColorUIResource getPrimary3() {
        return getWhite();
    }

    @Override
    protected ColorUIResource getSecondary2() {
        return getWhite();
    }

    @Override
    public void addCustomEntriesToTable(UIDefaults table) {
        super.addCustomEntriesToTable(table);
        List<Object> allWhiteGradient = Arrays.asList(1, 0, getWhite(), getWhite(), getWhite());
        table.put("Button.gradient", allWhiteGradient);
        table.put("ScrollBar.gradient", allWhiteGradient);
        table.put("MenuBar.gradient", allWhiteGradient);
        table.put("RadioButton.gradient", allWhiteGradient);
        table.put("ToggleButton.gradient", allWhiteGradient);
        table.put("CheckBoxMenuItem.gradient", allWhiteGradient);
        table.put("CheckBox.gradient", allWhiteGradient);
        table.put("ComboBox.selectionForeground", Color.BLACK);
        table.put("RadioButtonMenuItem.gradient", allWhiteGradient);
    }

    @Override
    public String getName() {
        return "dark";
    }
}
