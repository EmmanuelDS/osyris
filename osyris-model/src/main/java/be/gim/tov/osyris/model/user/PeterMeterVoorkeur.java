package be.gim.tov.osyris.model.user;

import org.conscientia.api.model.ModelPropertyType;
import org.conscientia.api.model.StorableObject;
import org.conscientia.api.model.annotation.Description;
import org.conscientia.api.model.annotation.Label;
import org.conscientia.api.model.annotation.Model;
import org.conscientia.api.model.annotation.ModelClassName;
import org.conscientia.api.model.annotation.ModelStore;
import org.conscientia.api.model.annotation.Permission;
import org.conscientia.api.model.annotation.Permissions;
import org.conscientia.api.model.annotation.Type;
import org.conscientia.core.model.AbstractModelObject;

import be.gim.commons.resource.ResourceIdentifier;

/**
 * 
 * @author kristof
 * 
 */
@Model
@ModelStore("OsyrisDataStore")
@Label("Voorkeur")
@Description("Voorkeur")
@Permissions({
		@Permission(profile = "group:PeterMeter", action = "search", allow = true),
		@Permission(profile = "group:PeterMeter", action = "view", allow = true) })
public class PeterMeterVoorkeur extends AbstractModelObject implements
		StorableObject {

	// VARIABLES
	@Label("Trajectnaam")
	@Description("Trajectnaam")
	private String trajectNaam;

	@Label("Trajecttype")
	@Description("Trajecttype")
	private String trajectType;

	@Label("Maximale afstand")
	@Description("Maximale afstand")
	private float maxAfstand;

	@Label("Periode")
	@Description("Periode")
	private short periode;

	@Label("Gemeente")
	@Description("Gemeente")
	@ModelClassName("Gemeente")
	@Type(value = ModelPropertyType.RESOURCE_IDENTIFIER)
	private ResourceIdentifier gemeente;

	@Label("Regio")
	@Description("Regio")
	@ModelClassName("Regio")
	@Type(value = ModelPropertyType.RESOURCE_IDENTIFIER)
	private ResourceIdentifier regio;

	// GETTERS AND SETTERS
	public String getTrajectNaam() {
		return trajectNaam;
	}

	public void setTrajectNaam(String trajectNaam) {
		this.trajectNaam = trajectNaam;
	}

	public String getTrajectType() {
		return trajectType;
	}

	public void setTrajectType(String trajectType) {
		this.trajectType = trajectType;
	}

	public float getMaxAfstand() {
		return maxAfstand;
	}

	public void setMaxAfstand(float maxAfstand) {
		this.maxAfstand = maxAfstand;
	}

	public short getPeriode() {
		return periode;
	}

	public void setPeriode(short periode) {
		this.periode = periode;
	}

	public ResourceIdentifier getGemeente() {
		return gemeente;
	}

	public void setGemeente(ResourceIdentifier gemeente) {
		this.gemeente = gemeente;
	}

	public ResourceIdentifier getRegio() {
		return regio;
	}

	public void setRegio(ResourceIdentifier regio) {
		this.regio = regio;
	}
}