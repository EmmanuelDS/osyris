package be.gim.tov.osyris.model.controle;

import static org.conscientia.api.model.SubClassPersistence.UNION;

import org.conscientia.api.model.ModelPropertyType;
import org.conscientia.api.model.StorableObject;
import org.conscientia.api.model.annotation.ContentType;
import org.conscientia.api.model.annotation.Description;
import org.conscientia.api.model.annotation.Edit;
import org.conscientia.api.model.annotation.FileSize;
import org.conscientia.api.model.annotation.Label;
import org.conscientia.api.model.annotation.Model;
import org.conscientia.api.model.annotation.ModelStore;
import org.conscientia.api.model.annotation.NotSearchable;
import org.conscientia.api.model.annotation.Required;
import org.conscientia.api.model.annotation.SubClassPersistence;
import org.conscientia.api.model.annotation.Type;
import org.conscientia.api.model.annotation.View;
import org.conscientia.api.model.annotation.Width;
import org.conscientia.core.model.AbstractModelObject;

import be.gim.tov.osyris.model.controle.status.ProbleemStatus;

/**
 * 
 * @author kristof
 * 
 */
@Model
@ModelStore("OsyrisDataStore")
@SubClassPersistence(UNION)
@Label("Probleem")
@Edit(type = "probleem")
public abstract class Probleem extends AbstractModelObject implements
		StorableObject {

	// VARIABLES
	@Label("Status")
	@Description("Status")
	private ProbleemStatus status;

	@Required
	@NotSearchable
	@Label("Commentaar")
	@Description("Commentaar")
	@Type(value = ModelPropertyType.TEXT)
	private String commentaar;

	@Label("Foto")
	@Description("Foto")
	@View(type = "image")
	@NotSearchable
	@ContentType("image/*")
	@FileSize(2 * 1024 * 1024)
	@Width(250)
	private byte[] foto;

	// GETTERS AND SETTERS
	public ProbleemStatus getStatus() {
		return status;
	}

	public void setStatus(ProbleemStatus status) {
		this.status = status;
	}

	public String getCommentaar() {
		return commentaar;
	}

	public void setCommentaar(String commentaar) {
		this.commentaar = commentaar;
	}

	public byte[] getFoto() {
		return foto;
	}

	public void setFoto(byte[] foto) {
		this.foto = foto;
	}
}