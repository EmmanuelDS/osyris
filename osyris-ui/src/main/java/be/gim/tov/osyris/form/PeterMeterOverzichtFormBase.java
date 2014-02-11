package be.gim.tov.osyris.form;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.conscientia.api.document.Document;
import org.conscientia.api.group.Group;
import org.conscientia.api.mail.MailSender;
import org.conscientia.api.permission.Permission;
import org.conscientia.api.permission.Permissions;
import org.conscientia.api.preferences.Preferences;
import org.conscientia.api.repository.ModelRepository;
import org.conscientia.api.search.Query;
import org.conscientia.api.user.User;
import org.conscientia.api.user.UserProfile;
import org.conscientia.api.user.UserRepository;
import org.conscientia.core.form.AbstractListForm;
import org.conscientia.core.permission.DefaultPermission;
import org.conscientia.core.search.DefaultQuery;
import org.conscientia.core.search.QueryBuilder;
import org.conscientia.core.user.UserUtils;
import org.jboss.seam.security.Identity;

import be.gim.commons.filter.FilterUtils;
import be.gim.commons.localization.DefaultInternationalString;
import be.gim.commons.resource.ResourceName;
import be.gim.tov.osyris.model.codes.PeterMeterNaamCode;
import be.gim.tov.osyris.model.traject.Traject;
import be.gim.tov.osyris.model.user.PeterMeterProfiel;
import be.gim.tov.osyris.model.user.PeterMeterVoorkeur;
import be.gim.tov.osyris.model.user.status.PeterMeterStatus;

/**
 * 
 * @author kristof
 * 
 */
@Named
@ViewScoped
public class PeterMeterOverzichtFormBase extends AbstractListForm<User> {
	private static final long serialVersionUID = 7761265026167905576L;

	private static final Log LOG = LogFactory
			.getLog(PeterMeterOverzichtFormBase.class);

	private static final String PERIODE_LENTE = "1";
	private static final String PERIODE_ZOMER = "2";
	private static final String PERIODE_HERFST = "3";

	// VARIABLES
	@Inject
	protected UserRepository userRepository;
	@Inject
	protected Identity identity;
	@Inject
	protected Preferences preferences;
	@Inject
	protected MailSender mailSender;

	protected boolean hasErrors;

	// METHODS
	@PostConstruct
	public void init() throws IOException {
		search();
	}

	public boolean isHasErrors() {
		return hasErrors;
	}

	public void setHasErrors(boolean hasErrors) {
		this.hasErrors = hasErrors;
	}

	@Override
	public String getName() {
		return "User";
	}

	public User getUser() {
		return object;
	}

	public void setUser(User user) {
		this.object = user;
	}

	@Override
	protected Query transformQuery(Query query) {

		query = new DefaultQuery(query);

		query.addFilter(FilterUtils.or(FilterUtils.equal(
				"#PeterMeterProfiel/status", PeterMeterStatus.ACTIEF),
				FilterUtils.equal("#PeterMeterProfiel/status",
						PeterMeterStatus.KANDIDAAT), FilterUtils.equal(
						"#PeterMeterProfiel/status", PeterMeterStatus.PASSIEF)));

		return query;
	}

	/**
	 * Bewaren van nieuw aangemaakte PeterMeter.
	 * 
	 */
	@SuppressWarnings("unchecked")
	public void saveNewPeterMeter() {
		try {
			UserProfile userProfile = (UserProfile) object
					.getAspect("UserProfile");

			if (!checkUsernameExists(object.getUsername())) {

				// Generate password
				String password = RandomStringUtils.randomAlphanumeric(8);
				object.setPassword(password);

				// Document
				ResourceName name = UserUtils.getUserDocumentName(object
						.getUsername());

				Document<User> document = (Document<User>) modelRepository
						.createObject("Document", name);
				document.setName(name);
				document.setSearchable(false);

				String firstName = userProfile.getFirstName();
				String lastName = userProfile.getLastName();
				if (StringUtils.isNotBlank(firstName)
						|| StringUtils.isNotBlank(lastName)) {
					document.setTitle(new DefaultInternationalString(firstName
							+ " " + lastName));
				} else {
					document.setTitle(new DefaultInternationalString(object
							.getUsername()));
				}
				document.setOwner(name);
				document.set("object", object);

				// User
				object.putAspect(userProfile);

				PeterMeterProfiel profiel = (PeterMeterProfiel) object
						.getAspect("PeterMeterProfiel");
				if (profiel.getVoorkeuren().isEmpty()) {
					profiel.setVoorkeuren(null);
				}
				object.putAspect(profiel);

				// Set permissions
				setDocumentPermissions(name, document);

				// Assign to group
				ResourceName resourceName = new ResourceName(User.USER_SPACE,
						object.getUsername());
				addUserToPeterMeterGroup(resourceName);

				// Send mail
				sendCredentailsMail(
						object.getUsername(),
						object.getAspect("UserProfile").get("email").toString(),
						password);

				setHasErrors(false);
				modelRepository.saveDocument(document);
				modelRepository.saveObject(object);

				// Save new PM in codetabel
				PeterMeterNaamCode code = new PeterMeterNaamCode();
				code.setCode(resourceName.toString());
				code.setLabel(lastName + " " + firstName);
				modelRepository.saveObject(code);

				messages.info("Nieuwe peter/meter succesvol aangemaakt.");

				clear();
				search();

			} else {
				setHasErrors(true);
				messages.error("Gebruikersnaam bestaat al. Gelieve een andere gebruikersnaam te kiezen.");
			}
		} catch (IOException e) {
			LOG.error("Can not save model object.", e);
		} catch (Exception e) {
			messages.error("Peter/Meter niet bewaard: fout bij het versturen van confirmatie e-mail.");
			LOG.error("Can not send mail.", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Verwijderen van PeterMeter.
	 * 
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void delete() {
		try {
			// Delete trajectToewijzingen PM
			deleteToewijzingen();

			Permissions permissions = (Permissions) modelRepository.loadAspect(
					modelRepository.getModelClass("Permissions"),
					new ResourceName("user", object.getUsername()),
					FilterUtils.equal("scope", "document"));

			// Delete user from PM group
			Group group = (Group) modelRepository.loadObject(new ResourceName(
					"group", "PeterMeter"));
			group.getMembers().remove(modelRepository.getResourceName(object));
			modelRepository.saveObject(group);

			// Search and delete PeterMeterNaamCode
			QueryBuilder builder = new QueryBuilder("PeterMeterNaamCode");
			builder.addFilter(FilterUtils.equal("code",
					"user:" + object.getUsername()));

			List<PeterMeterNaamCode> codes = (List<PeterMeterNaamCode>) modelRepository
					.searchObjects(builder.build(), false, false);

			modelRepository.deleteObject(codes.get(0));

			// Delete user and document permissions
			modelRepository.deleteObject(object);

			if (permissions != null) {
				modelRepository.deleteAspect(permissions);
			}

			clear();
			search();
			messages.info("Peter/Meter succesvol verwijderd.");

		} catch (IOException e) {
			messages.error("Fout bij het verwijderen van Peter/Meter: "
					+ e.getMessage());
			LOG.error("Can not delete model object.", e);
		}
	}

	/**
	 * Sturen van email naar nieuwe PeterMeter met login credentials.
	 * 
	 * @param username
	 * @param email
	 * @param password
	 * @throws Exception
	 */
	private void sendCredentailsMail(String username, String email,
			String password) throws Exception {

		Map<String, Object> variables = new HashMap<String, Object>();
		variables.put("preferences", preferences);

		variables.put("username", username);
		variables.put("password", password);
		variables.put("group", userRepository.listGroupnames(username));

		mailSender.sendMail(
				preferences.getNoreplyEmail(),
				Collections.singleton(object.getAspect("UserProfile")
						.get("email").toString()),
				"/META-INF/resources/core/mails/newPeterMeter.fmt", variables);
	}

	/**
	 * Checken of gebruikersnaam reeds bestaat in het systeem.
	 * 
	 * @param username
	 * @return
	 */
	private boolean checkUsernameExists(String username) {

		try {
			Query query = new DefaultQuery("User");
			query.addFilter(FilterUtils.equal("username", username));
			if (modelRepository.countObjects(query, false) == 0) {
				return false;
			}
		} catch (IOException e) {
			LOG.error("Can not count objects.", e);
		}
		return true;

	}

	/**
	 * Toevoegen van Gebruiker aan PeterMeter groep.
	 * 
	 * @param resourceName
	 */
	@SuppressWarnings("unchecked")
	private void addUserToPeterMeterGroup(ResourceName resourceName) {

		try {
			Query query = new DefaultQuery("Group");
			query.addFilter(FilterUtils.equal("groupname", "PeterMeter"));
			List<Group> groups = new ArrayList<Group>();
			groups = (List<Group>) modelRepository.searchObjects(query, false,
					false);

			Group group = (Group) ModelRepository.getUniqueResult(groups);
			group.getMembers().add(resourceName);
			modelRepository.saveObject(group);

		} catch (IOException e) {
			LOG.error("Can not search objects.", e);
		}
	}

	/**
	 * Zetten van de juiste permissies op het PeterMeter document.
	 * 
	 * @param name
	 * @param document
	 */
	@SuppressWarnings("rawtypes")
	private void setDocumentPermissions(ResourceName name, Document document) {

		try {

			Permissions permissions = (Permissions) modelRepository.loadAspect(
					modelRepository.getModelClass("Permissions"), name,
					FilterUtils.equal("scope", "document"));

			if (permissions == null) {
				permissions = (Permissions) modelRepository.createAspect(
						modelRepository.getModelClass("Permissions"), name);
			}
			// Set scope
			permissions.set("scope", "document");

			// Add necessary permissions
			permissions.getPermissions().addAll(getAllowedPermissions(name));

			// Save permissions
			modelRepository.saveAspect(permissions);

		} catch (IOException e) {
			LOG.error("Can not load aspect.", e);
		} catch (InstantiationException e) {
			LOG.error("Can not instantiate aspect.", e);
		} catch (IllegalAccessException e) {
			LOG.error("Can not access aspect.", e);
		}
	}

	/**
	 * Ophalen toegelaten permissies.
	 * 
	 * @param name
	 * @return
	 */
	private List<Permission> getAllowedPermissions(ResourceName name) {

		List<Permission> permissions = new ArrayList<Permission>();

		permissions.add(new DefaultPermission(name, "view", true));
		permissions.add(new DefaultPermission(name, "edit", true));
		permissions.add(new DefaultPermission(new ResourceName("group",
				"Routedokter"), "view", true));
		permissions.add(new DefaultPermission(new ResourceName("group",
				"Routedokter"), "edit", true));
		permissions.add(new DefaultPermission(new ResourceName("group",
				"Medewerker"), "view", true));
		permissions.add(new DefaultPermission(new ResourceName("group",
				"Medewerker"), "edit", true));

		return permissions;
	}

	/**
	 * Verwijdert de trajectToewijzingen bij het deleten van PeterMeter.
	 * 
	 */
	@SuppressWarnings("unchecked")
	public void deleteToewijzingen() {

		try {
			ResourceName peterMeter = new ResourceName("user",
					object.getUsername());

			QueryBuilder builder = new QueryBuilder("Traject");
			builder.addFilter(FilterUtils.or(
					FilterUtils.equal("peterMeter1", peterMeter),
					FilterUtils.equal("peterMeter2", peterMeter),
					FilterUtils.equal("peterMeter3", peterMeter)));

			List<Traject> result = (List<Traject>) modelRepository
					.searchObjects(builder.build(), false, false);

			for (Traject traject : result) {
				if (traject.getPeterMeter1() != null
						&& traject.getPeterMeter1().equals(peterMeter)) {
					traject.setPeterMeter1(null);
				}
				if (traject.getPeterMeter2() != null
						&& traject.getPeterMeter2().equals(peterMeter)) {
					traject.setPeterMeter2(null);
				}
				if (traject.getPeterMeter3() != null
						&& traject.getPeterMeter3().equals(peterMeter)) {
					traject.setPeterMeter3(null);
				}

				modelRepository.saveObject(traject);
			}

		} catch (IOException e) {
			LOG.error("Can not search Trajecten.", e);
		}
	}

	@Override
	public void save() {

		try {

			if (checkVoorkeuren()) {

				hasErrors = false;
				modelRepository.saveObject(object);

				messages.info(documentMessages.documentSaveSuccess(ObjectUtils
						.toString(modelRepository.getResourceIdentifier(object))));

				clear();
				search();

			} else {
				hasErrors = true;
			}
		} catch (IOException e) {
			messages.error(documentMessages.documentSaveFailed(e.getMessage()));
			LOG.error("Can not save model object.", e);
		}
	}

	/**
	 * Checken of Voorkeuren voldoen aan de voorwaarden voor opslag.
	 * 
	 * @return
	 */
	private boolean checkVoorkeuren() {

		PeterMeterProfiel profiel = (PeterMeterProfiel) object
				.getAspect("PeterMeterProfiel");

		int counterLente = 0;
		int counterZomer = 0;
		int counterHerfst = 0;

		if (profiel.getVoorkeuren() != null
				&& !profiel.getVoorkeuren().isEmpty()) {

			for (PeterMeterVoorkeur voorkeur : profiel.getVoorkeuren()) {

				if (voorkeur.getPeriode().equals(PERIODE_LENTE)) {
					counterLente++;
				}
				if (voorkeur.getPeriode().equals(PERIODE_ZOMER)) {
					counterZomer++;
				}
				if (voorkeur.getPeriode().equals(PERIODE_HERFST)) {
					counterHerfst++;
				}

				if (voorkeur.getTrajectType().contains("Route")
						&& null == voorkeur.getTrajectNaam()) {
					messages.error("Bewaren profiel niet gelukt: Indien een voorkeur van het type 'route' opgegeven is, moet ook een trajectnaam ingevuld worden.");
					return false;
				}
			}

			if (counterLente > 3 || counterZomer > 3 || counterHerfst > 3) {
				messages.error("Bewaren profiel niet gelukt: Per periode zijn er maximaal 3 voorkeuren toegelaten.");
				return false;
			}
		}
		return true;
	}
}
