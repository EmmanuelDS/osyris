package be.gim.tov.osyris.model.support;

import org.conscientia.api.model.ModelObject;
import org.conscientia.api.model.annotation.For;
import org.conscientia.api.model.annotation.Handler;
import org.conscientia.core.handler.object.AbstractObjectSupport;

import be.gim.commons.localization.DefaultInternationalString;
import be.gim.commons.localization.InternationalString;
import be.gim.tov.osyris.model.traject.AutoRoute;
import be.gim.tov.osyris.model.traject.Traject;

@For("AutoRoute")
@Handler(type = "object")
public class AutoRouteSupport extends AbstractObjectSupport<AutoRoute> {

	@Override
	public InternationalString getLabel(ModelObject object) {
		Traject traject = (Traject) object;
		return new DefaultInternationalString(traject.getNaam());
	}
}
