package org.processmining.longdistancedependencies.plugins;

import java.awt.Desktop;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.plugins.InductiveMiner.ClassifierChooser;

import com.fluxicon.slickerbox.factory.SlickerFactory;

public class MiningDialog extends JPanel {

	private static final long serialVersionUID = 7693870370139578439L;
	private final JLabel doiLabel;
	private final JLabel doiValue;
	private final ClassifierChooser classifiers;
	private final String doi = "TODO: doi";

	public MiningDialog(XLog log) {
		SlickerFactory factory = SlickerFactory.instance();

		int gridy = 1;

		setLayout(new GridBagLayout());

		//classifiers
		{
			final JLabel classifierLabel = factory.createLabel("Event classifier");
			GridBagConstraints cClassifierLabel = new GridBagConstraints();
			cClassifierLabel.gridx = 0;
			cClassifierLabel.gridy = gridy;
			cClassifierLabel.weightx = 0.4;
			cClassifierLabel.anchor = GridBagConstraints.NORTHWEST;
			add(classifierLabel, cClassifierLabel);
		}

		classifiers = new ClassifierChooser(log);
		{
			GridBagConstraints cClassifiers = new GridBagConstraints();
			cClassifiers.gridx = 1;
			cClassifiers.gridy = gridy;
			cClassifiers.anchor = GridBagConstraints.NORTHWEST;
			cClassifiers.fill = GridBagConstraints.HORIZONTAL;
			cClassifiers.weightx = 0.6;
			add(classifiers, cClassifiers);
		}

		gridy++;

		//spacer
		{
			JLabel spacer = factory.createLabel(" ");
			GridBagConstraints cSpacer = new GridBagConstraints();
			cSpacer.gridx = 0;
			cSpacer.gridy = gridy;
			cSpacer.anchor = GridBagConstraints.WEST;
			add(spacer, cSpacer);
		}

		gridy++;

		//assumption
		{
			JLabel assumptionLabel = factory.createLabel("Assumption");
			GridBagConstraints cAssumptionLabel = new GridBagConstraints();
			cAssumptionLabel.gridx = 0;
			cAssumptionLabel.gridy = gridy;
			cAssumptionLabel.weightx = 0.4;
			cAssumptionLabel.anchor = GridBagConstraints.NORTHWEST;
			add(assumptionLabel, cAssumptionLabel);

			{
				JLabel assumptionValue = factory.createLabel(
						"<html>This plug-in ignores the final markings of the accepting Petri net<br>and instead assumes that every deadlock is a final marking, <br>and that a deadlock is always reachable.</html>");
				GridBagConstraints cAssumptionValue = new GridBagConstraints();
				cAssumptionValue.gridx = 1;
				cAssumptionValue.gridy = gridy;
				cAssumptionValue.anchor = GridBagConstraints.NORTHWEST;
				cAssumptionValue.weightx = 0.6;
				add(assumptionValue, cAssumptionValue);
			}
		}

		gridy++;

		//spacer
		{
			JLabel spacer = factory.createLabel(" ");
			GridBagConstraints cSpacer = new GridBagConstraints();
			cSpacer.gridx = 0;
			cSpacer.gridy = gridy;
			cSpacer.anchor = GridBagConstraints.WEST;
			add(spacer, cSpacer);
		}

		gridy++;

		//doi
		{
			doiLabel = factory.createLabel("More information");
			GridBagConstraints cDoiLabel = new GridBagConstraints();
			cDoiLabel.gridx = 0;
			cDoiLabel.gridy = gridy;
			cDoiLabel.weightx = 0.4;
			cDoiLabel.anchor = GridBagConstraints.NORTHWEST;
			add(doiLabel, cDoiLabel);
		}

		{
			doiValue = factory.createLabel("doi doi");
			GridBagConstraints cDoiValue = new GridBagConstraints();
			cDoiValue.gridx = 1;
			cDoiValue.gridy = gridy;
			cDoiValue.anchor = GridBagConstraints.NORTHWEST;
			cDoiValue.weightx = 0.6;
			add(doiValue, cDoiValue);
		}

		doiValue.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (doi != null) {
					openWebPage(doi);
				}
			}
		});
		doiValue.setText(doi);
	}

	public XEventClassifier getClassifier() {
		return classifiers.getSelectedClassifier();
	}

	public static void openWebPage(String url) {
		if (Desktop.isDesktopSupported()) {
			Desktop desktop = Desktop.getDesktop();
			try {
				desktop.browse(new URI(url));
			} catch (IOException | URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			Runtime runtime = Runtime.getRuntime();
			try {
				runtime.exec("xdg-open " + url);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
