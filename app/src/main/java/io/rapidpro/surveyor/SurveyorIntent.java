package io.rapidpro.surveyor;

public interface SurveyorIntent {
    String EXTRA_ORG_UUID = "surveyor.extra.org_uuid";
    String EXTRA_FLOW_UUID = "surveyor.extra.flow_uuid";

    String EXTRA_SUBMISSION_FILE = "surveyor.extra.submission_file";

    // where media files are to be stored
    String EXTRA_MEDIA_FILE = "surveyor.extra.media_file";

    String EXTRA_ERROR = "surveyor.extra.error";
    String EXTRA_CAMERA_DIRECTION = "surveyor.extra.camera_direction";
}

