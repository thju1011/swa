package de.shop.artikelverwaltung.rest;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.util.Collection;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.jboss.logging.Logger;

import de.shop.artikelverwaltung.domain.Artikel;
import de.shop.artikelverwaltung.service.ArtikelService;
import de.shop.util.Log;
import de.shop.util.NotFoundException;
import de.shop.util.Transactional;


@Path("/artikel")
@Produces(APPLICATION_JSON)
@Consumes
@RequestScoped
@Transactional
@Log
public class ArtikelResource {
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	
	@Inject
	private ArtikelService as;
	
	@PostConstruct
	private void postConstruct() {
		LOGGER.debugf("CDI-faehiges Bean %s wurde erzeugt", this);
	}
	
	@Inject
	private UriHelperArtikel uriHelperArtikel;
	
	@PreDestroy
	private void preDestroy() {
		LOGGER.debugf("CDI-faehiges Bean %s wird geloescht", this);
	}
	
	@GET
	@Path("{id:[1-9][0-9]*}")
	public Artikel findArtikel(@PathParam("id") Long id, @Context UriInfo uriInfo) {
		final Artikel artikel = as.findArtikelbyID(id);
		if (artikel == null) {
			final String msg = "Kein Artikel gefunden mit der ID " + id;
			throw new NotFoundException(msg);
		}

		return artikel;
	}
	
	@POST
	@Consumes(APPLICATION_JSON)
	@Produces
	public Response createArtikel(Artikel artikel, @Context UriInfo uriInfo, @Context HttpHeaders headers) {
		
		
		artikel = as.createArtikel(artikel);
		LOGGER.debugf("Kunde: %s", artikel);
	
		final URI artikelUri = uriHelperArtikel.getUriArtikel(artikel, uriInfo);
		return Response.created(artikelUri).build();
	}
	
	@GET
	@Path("/prefix/id/{id:[1-9][0-9]*}")
	public Collection<Long> findIdsByPrefix(@PathParam("id") String idPrefix) {
		final Collection<Long> ids = as.findIdsByPrefix(idPrefix);
		return ids;
	}
	
	@PUT
	@Consumes(APPLICATION_JSON)
	@Produces
	public void updateArtikel(Artikel artikel, @Context UriInfo uriInfo, @Context HttpHeaders headers) {
		// Vorhandenen Artikel ermitteln
		final Artikel origArtikel = as.findArtikelbyID(artikel.getAId());
		if (origArtikel == null) {

			final String msg = "Kein Artikel gefunden mit der ID " + artikel.getAId();
			throw new NotFoundException(msg);
		}
		LOGGER.debugf("Artikel vorher: %s", origArtikel);
	
		origArtikel.setValues(artikel);
		LOGGER.debugf("Artikel nachher: %s", origArtikel);
		
		artikel = as.updateArtikel(origArtikel);
		if (artikel == null) {

			final String msg = "Kein Artikel gefunden mit der ID " + origArtikel.getAId();
			throw new NotFoundException(msg);
		}
	}
	
}
