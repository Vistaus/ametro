/*
 * http://code.google.com/p/ametro/
 * Transport map viewer for Android platform
 * Copyright (C) 2009-2010 Roman.Golovanov@gmail.com and other
 * respective project committers (see project home page)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.ametro.model;

import java.util.Locale;

import org.ametro.model.ext.ModelLocation;
import org.ametro.model.storage.ModelBuilder;
import org.ametro.util.StringUtil;


public class Model {

	public final static String LOCALE_RU = "ru";
	public final static String LOCALE_EN = "en";

	public static final int VERSION = 1;
	
	public int countryName;
	public int cityName;
	public int[] authors;
	public int[] delays;
	public int[] comments;
	
	public ModelLocation location;
	
	public String systemName;
	public long timestamp;

	public String[] locales; // all available locales
	public String[][] localeTexts; // strings for all locales

	public String localeCurrent; // current locale
	public String[] texts; // strings for current locales
	public int textLength; // string table size
	public int textLengthDescription; // string table for descirption size

	//public ModelDescription description; // model description, description.csv

	public TransportMap[] maps;
	public TransportLine[] lines;
	public TransportSegment[] segments;
	public TransportTransfer[] transfers;
	public TransportStation[] stations;
	public TransportStationInfo[] stationInfos;

	public String[] viewNames;
	public MapView[] views;

	public String[] layerNames;
	public MapLayerContainer[] layers;

	public AbstractImage[] images;

	/****************** VOLATILE FIELDS ********************/
	public String fileSystemName;
	/********************** GETTERS ************************/
	
	public String getCountryName(){
		return Model.getLocalizedString(this,countryName);
	}
	
	public String getCityName(){
		return Model.getLocalizedString(this,cityName);
	}
	
	public String[] getAuthors(){
		return Model.getLocalizedStrings(this,authors);
	}
	
	public String[] getComments(){
		return Model.getLocalizedStrings(this,comments);
	}
	
	public String[] getDelays(){
		return Model.getLocalizedStrings(this,delays);
	}
	
	public String toString() {
		return "[NAME:" + systemName + ";COUNTRY:" + getCountryName() + ";CITY:" + getCityName() + ";LOCALES=" + "{" + StringUtil.join(locales,",")  + "}]";
	}

    
	
	/****************** LOCALIZATION **********************/
	public String getLocaleName(Locale locale){
		if(locale!=null){
			final int len = locales.length;
			final String languageCode = locale.getLanguage();
			for(int i = 0; i < len; i++){
				if( languageCode.equals(locales[i]) ){
					return locales[i];
				}
			}
		}
		return locales[0];
	}

	public int getLocaleId(Locale locale){
		if(locale!=null){
			final int len = locales.length;
			final String languageCode = locale.getLanguage();
			for(int i = 0; i < len; i++){
				if( languageCode.equals(locales[i]) ){
					return i;
				}
			}
		}
		return 0;
	}

	public Locale[] getAvailableLocales(){
		Locale[] res = new Locale[locales.length];
		final int len = locales.length;
		for(int i = 0; i < len; i++){
			res[i] = new Locale(locales[i]);
		}
		return res;
	}	

	public boolean isLocaleLoaded(Locale locale) {
		int id = getLocaleId(locale);
		return localeTexts[id]!=null;
	}

	
	public void setLocale(Locale locale){
		int id = getLocaleId(locale);
		String newLocale = locales[id];
		String[] newTexts = localeTexts[id];
		if(newTexts==null){
			String[] texts = ModelBuilder.loadModelLocale(fileSystemName, this, id);
			if(texts!=null){
				newTexts = localeTexts[id];
			}else{
				newLocale = locales[0];
				newTexts = localeTexts[0];
			}
		}
		localeCurrent = newLocale;
		texts = newTexts;
	}

	public void setLocaleTexts(Locale locale, String[] texts){
		Integer id = getLocaleId(locale);
		if(id!=null){
			localeTexts[id] = texts;
		}
	}

	/********************** STATIC METHODS ***********************/

	public static long getSegmentKey(long fromId, long toId) {
		long nodeKey = ( fromId << 32 ) + toId;
		return nodeKey;
	}

	public static String getLocalizedString(Model owner, int id) {
		return owner.texts[id];
	}

	public static String[] getLocalizedStrings(Model owner, int[] ids) {
		final int len = ids.length; 
		final String[] res = new String[len];
		final String[] texts = owner.texts;
		for(int i = 0; i < len; i++){
			res[i] = texts[i];
		}
		return res;
	}

	/*********************** OBJECT METHODS ***********************/

	public boolean completeEqual(Model other) {
		return locationEqual(other) 
		&& timestamp == other.timestamp;
	}

	public boolean locationEqual(Model other) {
		return getCountryName().equals(other.getCountryName())
		&& getCityName().equals(other.getCityName());
	}

	public static boolean isNullOrEmpty(Model model) {
		return model == null
			|| model.systemName == null
			|| model.timestamp == 0
			|| model.textLength == 0
			|| model.maps == null 
			|| model.stations == null 
			|| model.segments == null 
			|| model.lines == null 
			|| model.transfers == null 
			|| model.views == null
			|| model.layers == null
			|| model.locales == null
			|| model.localeTexts == null;
	}

	public static boolean isDescriptionLoaded(Model model) {
		return model != null;
	}
	
	
	public MapView getView(String name) {
		final Integer id = getViewId(name);
		if(id!=null){
			return views[id];
		}
		return null;
	}
	
	public MapView loadView(String name) {
		final Integer id = getViewId(name);
		if(id!=null){
			MapView v = views[id];
			if(v==null){
				v = ModelBuilder.loadModelView(fileSystemName, this, name);
			}
			return v;
		}
		return null;
	}

	public Integer getViewId(String viewName) {
		final int len = views.length;
		for(int i=0;i<len;i++){
			if(viewNames[i].equals(viewName)){
				return i;
			}
		}
		return null;
	}

	public MapView getDefaultView() {
		return views[0];
	}

	public TransportSegment getTransportSegment(int from, int to) {
		for(TransportSegment obj : segments){
			if(obj.stationFromId == from && obj.stationToId == to){
				return obj;
			}
		}
		return null;
	}

	public TransportTransfer getTransportTransfer(int to, int from) {
		for(TransportTransfer obj : transfers){
			if(obj.stationFromId == from && obj.stationToId == to){
				return obj;
			}
		}
		return null;
	}

	
}
