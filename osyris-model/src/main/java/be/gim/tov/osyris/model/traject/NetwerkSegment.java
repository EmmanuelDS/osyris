package be.gim.tov.osyris.model.traject;

import org.conscientia.api.model.ModelPropertyType;
import org.conscientia.api.model.annotation.Description;
import org.conscientia.api.model.annotation.Label;
import org.conscientia.api.model.annotation.LabelProperty;
import org.conscientia.api.model.annotation.Model;
import org.conscientia.api.model.annotation.ModelClassName;
import org.conscientia.api.model.annotation.ModelStore;
import org.conscientia.api.model.annotation.Type;
import org.conscientia.api.model.annotation.ValuesExpression;

import be.gim.commons.resource.ResourceIdentifier;

/**
 * 
 * @author kristof
 * 
 */
@Model
@ModelStore("OsyrisDataStore")
public abstract class NetwerkSegment extends Traject {

	// VARIABLES
	@Label("Enkele richting")
	@Description("Enkele richting")
	@Type(value = ModelPropertyType.ENUM)
	@ValuesExpression("#{osyrisModelFunctions.canonicalBoolean}")
	private String enkeleRichting;

	@Label("Van knooppunt")
	@Description("van knooppunt")
	@ModelClassName("NetwerkKnooppunt")
	private ResourceIdentifier vanKnooppunt;

	@Label("Naar knooppunt")
	@Description("Naar knooppunt")
	@ModelClassName("NetwerkKnooppunt")
	private ResourceIdentifier naarKnooppunt;

	// GETTERS AND SETTERS
	// Segmenten moeten label id krijgen
	@Override
	@LabelProperty
	public Long getId() {
		return (Long) super.getId();
	}

	public String getEnkeleRichting() {
		return enkeleRichting;
	}

	public void setEnkeleRichting(String enkeleRichting) {
		this.enkeleRichting = enkeleRichting;
	}

	public ResourceIdentifier getVanKnooppunt() {
		return vanKnooppunt;
	}

	public void setVanKnooppunt(ResourceIdentifier vanKnooppunt) {
		this.vanKnooppunt = vanKnooppunt;
	}

	public ResourceIdentifier getNaarKnooppunt() {
		return naarKnooppunt;
	}

	public void setNaarKnooppunt(ResourceIdentifier naarKnooppunt) {
		this.naarKnooppunt = naarKnooppunt;
	}
}