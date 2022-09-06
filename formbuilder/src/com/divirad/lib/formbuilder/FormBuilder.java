package com.divirad.lib.formbuilder;

import java.awt.Component;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class FormBuilder {
	private int attributeCount = 0;
	private JPanel work_pane;

	public interface IAddModifier {
		public void addMod(Component c);
	}

	public FormBuilder(JPanel pane) {
		work_pane = pane;
	}

	private JPanel pane() {
		return work_pane;
	}

	/**
	 * Add new pair of label and JTextField
	 * 
	 * @param label String to label the textfield with
	 */
	public Component cAdd(String label) {
		return cAdd(pane(), label, c -> {
		});
	}

	/**
	 * Add new pair of label and JTextField with modifiers
	 * 
	 * @param label String to label the textfield with
	 * @param am    function with modifiers for the textfield
	 */
	public Component cAdd(String label, IAddModifier am) {
		return cAdd(pane(), label, JTextField.class, am);
	}

	/**
	 * Add new pair of Label and Component
	 * 
	 * @param label String to label the component with
	 * @param c     Type of the component
	 */
	public Component cAdd(String label, Class<? extends Component> c) {
		return cAdd(pane(), label, c, cc -> {
		});
	}

	/**
	 * Add new pair of Label and Component with modifies
	 * 
	 * @param label String to label the component with
	 * @param c     Type of the component
	 * @param am    function with modifiers for the component
	 */
	public Component cAdd(String label, Class<? extends Component> c, IAddModifier am) {
		try {
			return cAdd(pane(), label, c.getDeclaredConstructor((Class[]) null).newInstance(), am);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Add new pair of Label and Component
	 * 
	 * @param label String to label the component with
	 * @param comp  component to add
	 */
	public Component cAdd(String label, Component comp) {
		return cAdd(pane(), label, comp, c -> {
		});
	}

	/**
	 * Add new pair of Label and Component
	 * 
	 * @param label String to label the component with
	 * @param comp  component to add
	 * @param am    function with modifiers for the component
	 */
	public Component cAdd(String label, Component comp, IAddModifier am) {
		return cAdd(pane(), label, comp, am);
	}

	/**
	 * Add new pair of label and JTextField to given JPanel
	 * 
	 * @param pane  JPanel to which the pair should be added
	 * @param label String to label the textfield with
	 */
	@SuppressWarnings("unused")
	private Component cAdd(JPanel pane, String label) {
		return cAdd(pane, label, c -> {
		});
	}

	/**
	 * Add new pair of label and JTextField with modifiers to given JPanel
	 * 
	 * @param pane  JPanel to which the pair should be added
	 * @param label String to label the textfield with
	 * @param am    function with modifiers for the textfield
	 */
	private Component cAdd(JPanel pane, String label, IAddModifier am) {
		return cAdd(pane, label, JTextField.class, am);
	}

	/**
	 * Add new pair of Label and Component to given JPanel
	 * 
	 * @param pane  JPanel to which the pair should be added
	 * @param label String to label the component with
	 * @param c     Type of the component
	 */
	@SuppressWarnings("unused")
	private Component cAdd(JPanel pane, String label, Class<? extends Component> c) {
		return cAdd(pane, label, c, cc -> {
		});
	}

	/**
	 * Add new pair of Label and Component with modifiers to given JPanel
	 * 
	 * @param pane  JPanel to which the pair should be added
	 * @param label String to label the component with
	 * @param c     Type of the component
	 * @param am    function with modifiers for the component
	 */
	private Component cAdd(JPanel pane, String label, Class<? extends Component> c, IAddModifier am) {
		try {
			return cAdd(pane, label, c.getDeclaredConstructor((Class[]) null).newInstance(), am);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Add new pair of Label and Component to given JPanel
	 * 
	 * @param pane  JPanel to which the pair should be added
	 * @param label String to label the component with
	 * @param comp  component to add
	 */
	@SuppressWarnings("unused")
	private Component cAdd(JPanel pane, String label, Component comp) {
		return cAdd(pane, label, comp, c -> {
		});
	}

	/**
	 * Add new pair of Label and Component to given JPanel
	 * 
	 * @param pane  JPanel to which the pair should be added
	 * @param label String to label the component with
	 * @param comp  component to add
	 * @param am    function with modifiers for the component
	 */
	private Component cAdd(JPanel pane, String label, Component comp, IAddModifier am) {
		JLabel l = new JLabel(label, JLabel.TRAILING);
		pane.add(l);
		l.setLabelFor(comp);
		am.addMod(comp);
		pane.add(comp);
		attributeCount++;
		return comp;
	}

	public void finish_form() {
		SpringUtilities.makeCompactGrid(pane(), attributeCount, 2, 6, 6, 6, 6);
	}

	int tabs = 0;

	/**
	 * Shows the structure of a Component (Container)
	 * 
	 * @param c Component to show the contents of
	 */
	public void showComponents(Component c) {
		for (int i = 0; i < tabs; i++)
			System.out.print("\t");
		System.out.println(c);
		if (!(c instanceof JPanel)) {
			tabs--;
			return;
		}
		for (Component cs : ((JPanel) c).getComponents()) {
			tabs++;
			showComponents(cs);
		}
		tabs--;
	}
}
