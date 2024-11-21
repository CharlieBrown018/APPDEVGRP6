package com.special;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.Component;
import java.util.function.Consumer;

public class ButtonEditor extends AbstractCellEditor implements TableCellEditor {
    protected JButton button;
    private String label;
    private boolean isPushed;
    private Consumer<Object[]> buttonClicked;
    private JTable table;

    public ButtonEditor(JTable table, Consumer<Object[]> buttonClickedListener) {
        this.button = new JButton();
        this.button.setOpaque(true);
        this.buttonClicked = buttonClickedListener;
        this.table = table;
        this.button.addActionListener(e -> stopCellEditing());
    }

    @Override
    public Object getCellEditorValue() {
        return label;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        if (isSelected) {
            button.setForeground(table.getSelectionForeground());
            button.setBackground(table.getSelectionBackground());
        } else {
            button.setForeground(table.getForeground());
            button.setBackground(table.getBackground());
        }
        label = (value == null) ? "" : value.toString();
        button.setText(label);
        isPushed = true;
        buttonClicked.accept(new Object[]{table.getValueAt(row, 0), table.getValueAt(row, 1), table.getValueAt(row, 2)});
        return button;
    }

    @Override
    public boolean stopCellEditing() {
        if (isPushed) {
            // Here you would put the code for deletion
            // For now, just reset the push state
            isPushed = false;
        }
        return super.stopCellEditing();
    }

    @Override
    protected void fireEditingStopped() {
        super.fireEditingStopped();
    }
}
