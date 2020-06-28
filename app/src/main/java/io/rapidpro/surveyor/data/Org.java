package io.rapidpro.surveyor.data;

import android.content.Context;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import io.rapidpro.surveyor.Logger;
import io.rapidpro.surveyor.R;
import io.rapidpro.surveyor.SurveyorApplication;
import io.rapidpro.surveyor.engine.OrgAssets;
import io.rapidpro.surveyor.net.TembaException;
import io.rapidpro.surveyor.net.TembaService;
import io.rapidpro.surveyor.net.responses.Boundary;
import io.rapidpro.surveyor.net.responses.Field;
import io.rapidpro.surveyor.net.responses.Group;
import io.rapidpro.surveyor.utils.JsonUtils;
import io.rapidpro.surveyor.utils.RawJson;
import okhttp3.OkHttpClient;

import static io.rapidpro.surveyor.extend.StaticMethods.getMD5;
import static io.rapidpro.surveyor.extend.StaticMethods.playNotification;

public class Org {
    /**
     * Contains the JSON representation of this org
     */
    private static final String DETAILS_FILE = "details.json";

    /**
     * Contains a goflow assets file with this org's flows, groups, fields etc
     */
    private static final String ASSETS_FILE = "assets.json";

    /**
     * Contains summaries of each flow available in this org
     */
    private static final String FLOWS_FILE = "flows.json";

    private String token;

    private String name;

    @SerializedName("primary_language")
    private String primaryLanguage;

    private String[] languages;

    private String timezone;

    private String country;

    @SerializedName("date_style")
    private String dateStyle;

    private boolean anon;

    private String legacySubmissionsDirectory;

    private transient File directory;

    private transient List<Flow> flows;

    /**
     * Creates an new empty org
     *
     * @param directory the directory
     * @param token     the API token
     * @return the org
     */
    public static Org create(File directory, String name, String token) throws IOException {
        directory.mkdirs();

        Org org = new Org();
        org.name = name;
        org.token = token;
        org.directory = directory;
        org.flows = new ArrayList<>();
        org.legacySubmissionsDirectory = null;

        FileUtils.writeStringToFile(new File(directory, DETAILS_FILE), "{\"name\":\"" + name + "\",\"token\":\"" + token + "\"}");
        FileUtils.writeStringToFile(new File(directory, FLOWS_FILE), "[]");
        return org;
    }

    /**
     * Loads an org from a directory
     *
     * @param directory the directory
     * @return the org
     */
    static Org load(File directory) throws IOException {
        if (!directory.exists() || !directory.isDirectory()) {
            throw new RuntimeException(directory.getPath() + " is not a valid org directory");
        }

        // read details.json
        String detailsJSON = FileUtils.readFileToString(new File(directory, DETAILS_FILE));
        Org org = JsonUtils.unmarshal(detailsJSON, Org.class);
        org.directory = directory;

        // read flows.json
        String flowsJson = FileUtils.readFileToString(new File(directory, FLOWS_FILE));

        TypeToken type = new TypeToken<List<Flow>>() {
        };
        org.flows = JsonUtils.unmarshal(flowsJson, type);
        return org;
    }

    /**
     * Gets the UUID of this org (i.e. the name of its directory)
     *
     * @return the UUID
     */
    public String getUuid() {
        return directory.getName();
    }

    /**
     * Gets the directory of this org
     *
     * @return the directory
     */
    public File getDirectory() {
        return directory;
    }

    /**
     * Gets the API token for this org
     *
     * @return the API token
     */
    public String getToken() {
        return token;
    }

    /**
     * Gets the name of this org
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the country code of this org
     *
     * @return the country code
     */
    public String getCountry() {
        return country;
    }

    public String getPrimaryLanguage() {
        return primaryLanguage;
    }

    public void setPrimaryLanguage(String language) {
        primaryLanguage = language;
    }

    public String[] getLanguages() {
        return languages;
    }

    public String getTimezone() {
        return timezone;
    }

    public String getDateStyle() {
        return dateStyle;
    }

    public boolean isAnon() {
        return anon;
    }

    /**
     * Gets the directory of legacy submissions for this org (may be null)
     *
     * @return the directory
     */
    public String getLegacySubmissionsDirectory() {
        return legacySubmissionsDirectory;
    }

    public void setLegacySubmissionsDirectory(String legacySubmissionsDirectory) {
        this.legacySubmissionsDirectory = legacySubmissionsDirectory;
    }

    public List<Flow> getFlows() {
        return flows;
    }

    /**
     * Gets the flow with the given UUID
     *
     * @param uuid the flow UUID
     * @return the flow or null if no such flow exists
     */
    public Flow getFlow(String uuid) {
        for (Flow flow : flows) {
            if (flow.getUuid().equals(uuid)) {
                return flow;
            }
        }
        return null;
    }

    /**
     * Gets whether this org has downloaded assets
     *
     * @return true if org has assets
     */
    public boolean hasAssets() {
        return new File(directory, ASSETS_FILE).exists();
    }

    /**
     * Gets this org's downloaded assets
     *
     * @return the assets JSON
     */
    public String getAssets() throws IOException {
        return FileUtils.readFileToString(new File(directory, ASSETS_FILE));
    }

    public String initialLanguage;
    /**
     * Refreshes this org from RapidPro
     */
    public void refresh(boolean includeAssets, RefreshProgress progress) throws TembaException, IOException {
        TembaService svc = SurveyorApplication.get().getTembaService();
        io.rapidpro.surveyor.net.responses.Org apiOrg = svc.getOrg(this.token);

        this.name = apiOrg.getName();
        this.initialLanguage = apiOrg.getPrimaryLanguage();
        this.primaryLanguage = apiOrg.getPrimaryLanguage();

        this.languages = apiOrg.getLanguages();
        this.timezone = apiOrg.getTimezone();
        this.country = apiOrg.getCountry();
        this.dateStyle = apiOrg.getDateStyle();
        this.anon = apiOrg.isAnon();
        this.save();

        if (progress != null) {
            progress.reportProgress(5);
        }

        if (includeAssets) {
            refreshAssets(progress);
        }
    }

    public void save() throws IOException {
        // (re)write org fields to details.json
        String detailsJSON = JsonUtils.marshal(this);
        FileUtils.writeStringToFile(new File(directory, DETAILS_FILE), detailsJSON);
    }

    int doneFile;
    int fileCount;
    private void refreshAssets(RefreshProgress progress) throws TembaException, IOException {
        List<Field> fields = SurveyorApplication.get().getTembaService().getFields(getToken());

        progress.reportProgress(10);

        List<Group> groups = SurveyorApplication.get().getTembaService().getGroups(getToken());

        progress.reportProgress(15);

        List<io.rapidpro.surveyor.net.responses.Flow> flows = SurveyorApplication.get().getTembaService().getFlows(getToken());

        progress.reportProgress(20);

        List<RawJson> definitions = SurveyorApplication.get().getTembaService().getDefinitions(getToken(), flows);

        // Download Files
        progress.reportMessage("Refreshing organization details..");

        fileCount = definitions.size();
        doneFile = 0;
        int filePercent = 0;
        int perFile =  (int) (60 / (float)fileCount);
        for(RawJson df: definitions){
            progress.reportMessage("Loading Flow Contents: " + (doneFile+1) + " / " + fileCount);
            progress.reportProgress(20 + filePercent);
            downloadMedia(df, progress);
            doneFile++;

            filePercent = filePercent + perFile;
        }

        progress.reportMessage("Refreshing organization details..");

        progress.reportProgress(80);

        List<Boundary> boundaries = SurveyorApplication.get().getTembaService().getBoundaries(getToken());

        progress.reportProgress(85);

        OrgAssets assets = OrgAssets.fromTemba(fields, groups, boundaries, definitions);
        String assetsJSON = JsonUtils.marshal(assets);

        FileUtils.writeStringToFile(new File(directory, ASSETS_FILE), assetsJSON);

        progress.reportProgress(90);

        // update the flow summaries
        this.flows.clear();
        this.flows.addAll(assets.getFlows());

        // and write that to flows.json as well
        String summariesJSON = JsonUtils.marshal(this.flows);
        FileUtils.writeStringToFile(new File(directory, FLOWS_FILE), summariesJSON);

        progress.reportProgress(100);

        Logger.d("Refreshed assets for org " + getUuid() + " (flows=" + flows.size() + ", fields=" + fields.size() + ", groups=" + groups.size() + ")");
    }

    private void downloadMedia(RawJson definition, RefreshProgress progress){
        String flow_name = "";
        JsonObject def = new JsonParser().parse(definition.toString()).getAsJsonObject();
        if(def.get("name") != null){
            flow_name = def.get("name").getAsString();
        }
        if(def.get("nodes") != null){
            JsonArray nodes = def.get("nodes").getAsJsonArray();
            for(int i = 0; i < nodes.size(); i++){
                if(nodes.get(i) != null){
                    // Multiple Actions
                    JsonObject node = nodes.get(i).getAsJsonObject();
                    if(nodes.get(i).getAsJsonObject().get("actions") != null){
                        JsonArray actions = node.get("actions").getAsJsonArray();
                        for(int j = 0; j < actions.size(); j++){
                            if(actions.get(j).getAsJsonObject().get("attachments") != null) {
                                // Contains Attachments
                                JsonArray attachments = actions.get(j).getAsJsonObject().get("attachments").getAsJsonArray();
                                for (int k = 0; k < attachments.size(); k++) {
                                    String attachment_data = attachments.get(k).getAsString();
                                    String attachment_uri = "";
                                    Log.e("ATTACHMENTS", attachment_data);

                                    int x = attachment_data.indexOf(":");
                                    attachment_uri = attachment_data.substring(x+1);
                                    String file_name = "flow_asset_" + getMD5(attachment_uri);
                                    URL url = null;
                                    try {
                                        url = new URL(attachment_uri);
                                        FileUtils.copyURLToFile(url, new File(directory.getAbsolutePath() + "/" + file_name));
                                        progress.reportMessage("Loading Flow Contents: " + (doneFile+1) + " / " + fileCount +
                                                                "\nFlow: " + flow_name +
                                                                "\nURL: " + url.getHost());
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

    }

    public interface RefreshProgress {
        void reportProgress(int percent);
        void reportMessage(String message);
    }
}
