package net.minecraft.launcher.ui.bottombar;

import javax.swing.*;
import java.awt.*;

public abstract class SidebarGridForm extends JPanel
{
    protected SidebarGridForm() {
    }
    
    protected void createInterface() {
        final GridBagLayout layout = new GridBagLayout();
        final GridBagConstraints constraints = new GridBagConstraints();
        this.setLayout(layout);
        this.populateGrid(constraints);
    }
    
    protected abstract void populateGrid(final GridBagConstraints p0);
    
    protected <T extends Component> T add(final T component, final GridBagConstraints constraints, final int x, final int y, final int weight, final int width) {
        return this.add(component, constraints, x, y, weight, width, 10);
    }
    
    protected <T extends Component> T add(final T component, final GridBagConstraints constraints, final int x, final int y, final int weight, final int width, final int anchor) {
        constraints.gridx = x;
        constraints.gridy = y;
        constraints.weightx = weight;
        constraints.weighty = 1.0;
        constraints.gridwidth = width;
        constraints.anchor = anchor;
        this.add(component, constraints);
        return component;
    }
}
