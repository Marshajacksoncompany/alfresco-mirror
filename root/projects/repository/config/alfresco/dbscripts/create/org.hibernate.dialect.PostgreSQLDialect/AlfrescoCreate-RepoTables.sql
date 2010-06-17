--
-- Title:      Core Repository Tables
-- Database:   PostgreSQL
-- Since:      V3.3 Schema 4000
-- Author:     unknown
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--

CREATE TABLE alf_applied_patch
(
    id VARCHAR(64) NOT NULL,
    description VARCHAR(1024),
    fixes_from_schema INT4,
    fixes_to_schema INT4,
    applied_to_schema INT4,
    target_schema INT4,
    applied_on_date TIMESTAMP,
    applied_to_server VARCHAR(64),
    was_executed BOOL,
    succeeded BOOL,
    report VARCHAR(1024),
    PRIMARY KEY (id)
);

CREATE TABLE alf_namespace
(
    id INT8 NOT NULL,
    version INT8 NOT NULL,
    uri VARCHAR(100) NOT NULL,
    PRIMARY KEY (id)
);

CREATE UNIQUE INDEX uri ON alf_namespace (uri);
CREATE SEQUENCE alf_namespace_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE alf_qname
(
    id INT8 NOT NULL,
    version INT8 NOT NULL,
    ns_id INT8 NOT NULL,
    local_name VARCHAR(200) NOT NULL,
    CONSTRAINT fk_alf_qname_ns FOREIGN KEY (ns_id) REFERENCES alf_namespace (id),    
    PRIMARY KEY (id)
);
CREATE UNIQUE INDEX ns_id ON alf_qname (ns_id, local_name);
CREATE INDEX fk_alf_qname_ns ON alf_qname (ns_id);
CREATE SEQUENCE alf_qname_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE alf_permission
(
    id INT8 NOT NULL,
    version INT8 NOT NULL,
    type_qname_id INT8 NOT NULL,
    name VARCHAR(100) NOT NULL,
    PRIMARY KEY (id),    
    CONSTRAINT fk_alf_perm_tqn FOREIGN KEY (type_qname_id) REFERENCES alf_qname (id)
);
CREATE UNIQUE INDEX type_qname_id ON alf_permission (type_qname_id, name);
CREATE INDEX fk_alf_perm_tqn ON alf_permission (type_qname_id);
CREATE SEQUENCE alf_permission_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE alf_ace_context
(
    id INT8 NOT NULL,
    version INT8 NOT NULL,
    class_context VARCHAR(1024),
    property_context VARCHAR(1024),
    kvp_context VARCHAR(1024),
    PRIMARY KEY (id)
);
CREATE SEQUENCE alf_ace_context_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE alf_authority
(
    id INT8 NOT NULL,
    version INT8 NOT NULL,
    authority VARCHAR(100),
    crc INT8,
    PRIMARY KEY (id)
);
CREATE UNIQUE INDEX authority ON alf_authority (authority, crc);
CREATE INDEX idx_alf_auth_aut ON alf_authority (authority);
CREATE SEQUENCE alf_authority_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE alf_access_control_entry
(
    id INT8 NOT NULL,
    version INT8 NOT NULL,
    permission_id INT8 NOT NULL,
    authority_id INT8 NOT NULL,
    allowed BOOL NOT NULL,
    applies INT4 NOT NULL,
    context_id INT8,
    PRIMARY KEY (id),    
    CONSTRAINT fk_alf_ace_auth FOREIGN KEY (authority_id) REFERENCES alf_authority (id),
    CONSTRAINT fk_alf_ace_ctx FOREIGN KEY (context_id) REFERENCES alf_ace_context (id),
    CONSTRAINT fk_alf_ace_perm FOREIGN KEY (permission_id) REFERENCES alf_permission (id)
);
CREATE UNIQUE INDEX permission_id ON alf_access_control_entry (permission_id, authority_id, allowed, applies, context_id);
CREATE INDEX fk_alf_ace_ctx ON alf_access_control_entry (context_id);
CREATE INDEX fk_alf_ace_perm ON alf_access_control_entry (permission_id);
CREATE INDEX fk_alf_ace_auth ON alf_access_control_entry (authority_id);
CREATE SEQUENCE alf_access_control_entry_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE alf_acl_change_set
(
    id INT8 NOT NULL,
    version INT8 NOT NULL,
    PRIMARY KEY (id)
);
CREATE SEQUENCE alf_acl_change_set_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE alf_access_control_list
(
    id INT8 NOT NULL,
    version INT8 NOT NULL,
    acl_id VARCHAR(36)  NOT NULL,
    latest BOOL NOT NULL,
    acl_version INT8 NOT NULL,
    inherits BOOL NOT NULL,
    inherits_from INT8,
    type INT4 NOT NULL,
    inherited_acl INT8,
    is_versioned BOOL NOT NULL,
    requires_version BOOL NOT NULL,
    acl_change_set INT8,
    PRIMARY KEY (id),
    CONSTRAINT fk_alf_acl_acs FOREIGN KEY (acl_change_set) REFERENCES alf_acl_change_set (id)
);
CREATE UNIQUE INDEX acl_id ON alf_access_control_list (acl_id, latest, acl_version);
CREATE INDEX idx_alf_acl_inh ON alf_access_control_list (inherits, inherits_from);
CREATE INDEX fk_alf_acl_acs ON alf_access_control_list (acl_change_set);
CREATE SEQUENCE alf_access_control_list_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE alf_acl_member
(
    id INT8 NOT NULL,
    version INT8 NOT NULL,
    acl_id INT8 NOT NULL,
    ace_id INT8 NOT NULL,
    pos INT4 NOT NULL,
    PRIMARY KEY (id),    
    CONSTRAINT fk_alf_aclm_ace FOREIGN KEY (ace_id) REFERENCES alf_access_control_entry (id),
    CONSTRAINT fk_alf_aclm_acl FOREIGN KEY (acl_id) REFERENCES alf_access_control_list (id)
);
CREATE UNIQUE INDEX aclm_acl_id ON alf_acl_member (acl_id, ace_id, pos);
CREATE INDEX fk_alf_aclm_acl ON alf_acl_member (acl_id);
CREATE INDEX fk_alf_aclm_ace ON alf_acl_member (ace_id);
CREATE SEQUENCE alf_acl_member_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE alf_authority_alias
(
    id INT8 NOT NULL,
    version INT8 NOT NULL,
    auth_id INT8 NOT NULL,
    alias_id INT8 NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_alf_autha_aut FOREIGN KEY (auth_id) REFERENCES alf_authority (id),
    CONSTRAINT fk_alf_autha_ali FOREIGN KEY (alias_id) REFERENCES alf_authority (id)
);
CREATE UNIQUE INDEX auth_id ON alf_authority_alias (auth_id, alias_id);
CREATE INDEX fk_alf_autha_ali ON alf_authority_alias (alias_id);
CREATE INDEX fk_alf_autha_aut ON alf_authority_alias (auth_id);
CREATE SEQUENCE alf_authority_alias_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE alf_activity_feed
(
    id INT8 NOT NULL,
    post_id INT8,
    post_date TIMESTAMP NOT NULL,
    activity_summary VARCHAR(1024),
    feed_user_id VARCHAR(255),
    activity_type VARCHAR(255) NOT NULL,
    activity_format VARCHAR(10),
    site_network VARCHAR(255),
    app_tool VARCHAR(36),
    post_user_id VARCHAR(255) NOT NULL,
    feed_date TIMESTAMP NOT NULL,
    PRIMARY KEY (id)
);
CREATE INDEX feed_postdate_idx ON alf_activity_feed (post_date);
CREATE INDEX feed_postuserid_idx ON alf_activity_feed (post_user_id);
CREATE INDEX feed_feeduserid_idx ON alf_activity_feed (feed_user_id);
CREATE INDEX feed_sitenetwork_idx ON alf_activity_feed (site_network);
CREATE INDEX feed_activityformat_idx ON alf_activity_feed (activity_format);
CREATE SEQUENCE alf_activity_feed_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE alf_activity_feed_control
(
    id INT8 NOT NULL,
    feed_user_id VARCHAR(255) NOT NULL,
    site_network VARCHAR(255),
    app_tool VARCHAR(36),
    last_modified TIMESTAMP NOT NULL,
    PRIMARY KEY (id)    
);
CREATE INDEX feedctrl_feeduserid_idx ON alf_activity_feed_control (feed_user_id);
CREATE SEQUENCE alf_activity_feed_control_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE alf_activity_post
(
    sequence_id INT8 NOT NULL,
    post_date TIMESTAMP NOT NULL,
    status VARCHAR(10) NOT NULL,
    activity_data VARCHAR(1024) NOT NULL,
    post_user_id VARCHAR(255) NOT NULL,
    job_task_node INT4 NOT NULL,
    site_network VARCHAR(255),
    app_tool VARCHAR(36),
    activity_type VARCHAR(255) NOT NULL,
    last_modified TIMESTAMP NOT NULL,
    PRIMARY KEY (sequence_id)
);
CREATE INDEX post_jobtasknode_idx ON alf_activity_post (job_task_node);
CREATE INDEX post_status_idx ON alf_activity_post (status);
CREATE SEQUENCE alf_activity_post_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE alf_audit_config
(
    id INT8 NOT NULL,
    config_url VARCHAR(1024) NOT NULL,
    PRIMARY KEY (id)
);
CREATE SEQUENCE alf_audit_config_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE alf_audit_date
(
    id INT8 NOT NULL,
    date_only DATE NOT NULL,
    day_of_year INT4 NOT NULL,
    day_of_month INT4 NOT NULL,
    day_of_week INT4 NOT NULL,
    week_of_year INT4 NOT NULL,
    week_of_month INT4 NOT NULL,
    month INT4 NOT NULL,
    quarter INT4 NOT NULL,
    half_year INT4 NOT NULL,
    full_year INT4 NOT NULL,
    PRIMARY KEY (id)
);
CREATE INDEX idx_alf_adtd_woy ON alf_audit_date (week_of_year);
CREATE INDEX idx_alf_adtd_fy ON alf_audit_date (full_year);
CREATE INDEX idx_alf_adtd_q ON alf_audit_date (quarter);
CREATE INDEX idx_alf_adtd_wom ON alf_audit_date (week_of_month);
CREATE INDEX idx_alf_adtd_dom ON alf_audit_date (day_of_month);
CREATE INDEX idx_alf_adtd_doy ON alf_audit_date (day_of_year);
CREATE INDEX idx_alf_adtd_dow ON alf_audit_date (day_of_week);
CREATE INDEX idx_alf_adtd_m ON alf_audit_date (month);
CREATE INDEX idx_alf_adtd_hy ON alf_audit_date (half_year);
CREATE INDEX idx_alf_adtd_dat ON alf_audit_date (date_only);
CREATE SEQUENCE alf_audit_date_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE alf_audit_source
(
    id INT8 NOT NULL,
    application VARCHAR(255) NOT NULL,
    service VARCHAR(255),
    method VARCHAR(255),
    PRIMARY KEY (id)
);
CREATE INDEX idx_alf_adts_met ON alf_audit_source (method);
CREATE INDEX idx_alf_adts_ser ON alf_audit_source (service);
CREATE INDEX idx_alf_adts_app ON alf_audit_source (application);
CREATE SEQUENCE alf_audit_source_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE alf_audit_fact
(
    id INT8 NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    transaction_id VARCHAR(56) NOT NULL,
    session_id VARCHAR(56),
    store_protocol VARCHAR(50),
    store_id VARCHAR(100),
    node_uuid VARCHAR(36),
    path VARCHAR(1024),
    filtered BOOL NOT NULL,
    return_val VARCHAR(1024),
    arg_1 VARCHAR(1024),
    arg_2 VARCHAR(1024),
    arg_3 VARCHAR(1024),
    arg_4 VARCHAR(1024),
    arg_5 VARCHAR(1024),
    fail BOOL NOT NULL,
    serialized_url VARCHAR(1024),
    exception_message VARCHAR(1024),
    host_address VARCHAR(1024),
    client_address VARCHAR(1024),
    message_text VARCHAR(1024),
    audit_date_id INT8 NOT NULL,
    audit_conf_id INT8 NOT NULL,
    audit_source_id INT8 NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_alf_adtf_conf FOREIGN KEY (audit_conf_id) REFERENCES alf_audit_config (id),
    CONSTRAINT fk_alf_adtf_date FOREIGN KEY (audit_date_id) REFERENCES alf_audit_date (id),
    CONSTRAINT fk_alf_adtf_src FOREIGN KEY (audit_source_id) REFERENCES alf_audit_source (id)
);
CREATE INDEX idx_alf_adtf_ref ON alf_audit_fact (store_protocol, store_id, node_uuid);
CREATE INDEX idx_alf_adtf_usr ON alf_audit_fact (user_id);
CREATE INDEX fk_alf_adtf_src ON alf_audit_fact (audit_source_id);
CREATE INDEX fk_alf_adtf_date ON alf_audit_fact (audit_date_id);
CREATE INDEX fk_alf_adtf_conf ON alf_audit_fact (audit_conf_id);
CREATE INDEX idx_alf_adtf_pth ON alf_audit_fact (path);
CREATE SEQUENCE alf_audit_fact_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE alf_server
(
    id INT8 NOT NULL,
    version INT8 NOT NULL,
    ip_address VARCHAR(39) NOT NULL,
    PRIMARY KEY (id)
);
CREATE UNIQUE INDEX ip_address ON alf_server (ip_address);
CREATE SEQUENCE alf_server_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE alf_transaction
(
    id INT8 NOT NULL,
    version INT8 NOT NULL,
    server_id INT8,
    change_txn_id VARCHAR(56) NOT NULL,
    commit_time_ms INT8,
    PRIMARY KEY (id),
    CONSTRAINT fk_alf_txn_svr FOREIGN KEY (server_id) REFERENCES alf_server (id)
);
CREATE INDEX idx_alf_txn_ctms ON alf_transaction (commit_time_ms);
CREATE INDEX fk_alf_txn_svr ON alf_transaction (server_id);
CREATE SEQUENCE alf_transaction_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE alf_store
(
    id INT8 NOT NULL,
    version INT8 NOT NULL,
    protocol VARCHAR(50) NOT NULL,
    identifier VARCHAR(100) NOT NULL,
    root_node_id INT8,
    PRIMARY KEY (id)
);
CREATE UNIQUE INDEX protocol ON alf_store (protocol, identifier);
CREATE SEQUENCE alf_store_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE alf_node
(
    id INT8 NOT NULL,
    version INT8 NOT NULL,
    store_id INT8 NOT NULL,
    uuid VARCHAR(36) NOT NULL,
    transaction_id INT8 NOT NULL,
    node_deleted BOOL NOT NULL,
    type_qname_id INT8 NOT NULL,
    acl_id INT8,
    audit_creator VARCHAR(255),
    audit_created VARCHAR(30),
    audit_modifier VARCHAR(255),
    audit_modified VARCHAR(30),
    audit_accessed VARCHAR(30),
    PRIMARY KEY (id),
    CONSTRAINT fk_alf_node_acl FOREIGN KEY (acl_id) REFERENCES alf_access_control_list (id),
    CONSTRAINT fk_alf_node_store FOREIGN KEY (store_id) REFERENCES alf_store (id),
    CONSTRAINT fk_alf_node_tqn FOREIGN KEY (type_qname_id) REFERENCES alf_qname (id),
    CONSTRAINT fk_alf_node_txn FOREIGN KEY (transaction_id) REFERENCES alf_transaction (id)
);
CREATE UNIQUE INDEX store_id ON alf_node (store_id, uuid);
CREATE INDEX idx_alf_node_del ON alf_node (node_deleted);
CREATE INDEX fk_alf_node_acl ON alf_node (acl_id);
CREATE INDEX fk_alf_node_txn ON alf_node (transaction_id);
CREATE INDEX fk_alf_node_store ON alf_node (store_id);
CREATE INDEX fk_alf_node_tqn ON alf_node (type_qname_id);
CREATE SEQUENCE alf_node_seq START WITH 1 INCREMENT BY 1;

CREATE INDEX fk_alf_store_root ON alf_store (root_node_id);
ALTER TABLE alf_store ADD CONSTRAINT fk_alf_store_root FOREIGN KEY (root_node_id) REFERENCES alf_node (id);

CREATE TABLE alf_child_assoc
(
    id INT8 NOT NULL,
    version INT8 NOT NULL,
    parent_node_id INT8 NOT NULL,
    type_qname_id INT8 NOT NULL,
    child_node_name_crc INT8 NOT NULL,
    child_node_name VARCHAR(50) NOT NULL,
    child_node_id INT8 NOT NULL,
    qname_ns_id INT8 NOT NULL,
    qname_localname VARCHAR(255) NOT NULL,
    qname_crc INT8 NOT NULL,
    is_primary BOOL,
    assoc_index INT4,
    PRIMARY KEY (id),    
    CONSTRAINT fk_alf_cass_cnode FOREIGN KEY (child_node_id) REFERENCES alf_node (id),
    CONSTRAINT fk_alf_cass_pnode FOREIGN KEY (parent_node_id) REFERENCES alf_node (id),
    CONSTRAINT fk_alf_cass_qnns FOREIGN KEY (qname_ns_id) REFERENCES alf_namespace (id),
    CONSTRAINT fk_alf_cass_tqn FOREIGN KEY (type_qname_id) REFERENCES alf_qname (id)
);
CREATE UNIQUE INDEX parent_node_id ON alf_child_assoc (parent_node_id, type_qname_id, child_node_name_crc, child_node_name);
CREATE INDEX fk_alf_cass_pnode ON alf_child_assoc (parent_node_id);
CREATE INDEX fk_alf_cass_cnode ON alf_child_assoc (child_node_id);
CREATE INDEX fk_alf_cass_tqn ON alf_child_assoc (type_qname_id);
CREATE INDEX fk_alf_cass_qnns ON alf_child_assoc (qname_ns_id);
CREATE INDEX idx_alf_cass_qncrc ON alf_child_assoc (qname_crc, type_qname_id, parent_node_id);
CREATE SEQUENCE alf_child_assoc_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE alf_locale
(
    id INT8 NOT NULL,
    version INT8 NOT NULL,
    locale_str VARCHAR(20) NOT NULL,
    PRIMARY KEY (id)    
);
CREATE UNIQUE INDEX locale_str ON alf_locale (locale_str);
CREATE SEQUENCE alf_locale_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE alf_attributes
(
    id INT8 NOT NULL,
    type VARCHAR(1) NOT NULL,
    version INT8 NOT NULL,
    acl_id INT8,
    bool_value BOOL,
    byte_value INT2,
    short_value INT4,
    int_value INT4,
    long_value INT8,
    float_value FLOAT4,
    double_value FLOAT8,
    string_value VARCHAR(1024),
    serializable_value BYTEA,
    PRIMARY KEY (id),    
    CONSTRAINT fk_alf_attr_acl FOREIGN KEY (acl_id) REFERENCES alf_access_control_list (id)
);
CREATE INDEX fk_alf_attr_acl ON alf_attributes (acl_id);
CREATE SEQUENCE alf_attributes_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE alf_global_attributes
(
    name VARCHAR(160) NOT NULL,
    attribute INT8,
    PRIMARY KEY (name),   
    CONSTRAINT fk_alf_gatt_att FOREIGN KEY (attribute) REFERENCES alf_attributes (id)
);
CREATE UNIQUE INDEX attribute ON alf_global_attributes (attribute);
CREATE INDEX fk_alf_gatt_att ON alf_global_attributes (attribute);

CREATE TABLE alf_list_attribute_entries
(
    list_id INT8 NOT NULL,
    mindex INT4 NOT NULL,
    attribute_id INT8,
    PRIMARY KEY (list_id, mindex),
    CONSTRAINT fk_alf_lent_att FOREIGN KEY (attribute_id) REFERENCES alf_attributes (id),
    CONSTRAINT fk_alf_lent_latt FOREIGN KEY (list_id) REFERENCES alf_attributes (id)
);
CREATE INDEX fk_alf_lent_att ON alf_list_attribute_entries (attribute_id);
CREATE INDEX fk_alf_lent_latt ON alf_list_attribute_entries (list_id);

CREATE TABLE alf_map_attribute_entries
(
    map_id INT8 NOT NULL,
    mkey VARCHAR(160) NOT NULL,
    attribute_id INT8,
    PRIMARY KEY (map_id, mkey),
    CONSTRAINT fk_alf_matt_att FOREIGN KEY (attribute_id) REFERENCES alf_attributes (id),
    CONSTRAINT fk_alf_matt_matt FOREIGN KEY (map_id) REFERENCES alf_attributes (id)
);
CREATE INDEX fk_alf_matt_matt ON alf_map_attribute_entries (map_id);
CREATE INDEX fk_alf_matt_att ON alf_map_attribute_entries (attribute_id);

CREATE TABLE alf_node_aspects
(
    node_id INT8 NOT NULL,
    qname_id INT8 NOT NULL,
    PRIMARY KEY (node_id, qname_id),
	CONSTRAINT fk_alf_nasp_n FOREIGN KEY (node_id) REFERENCES alf_node (id),
    CONSTRAINT fk_alf_nasp_qn FOREIGN KEY (qname_id) REFERENCES alf_qname (id)
);
CREATE INDEX fk_alf_nasp_n ON alf_node_aspects (node_id);
CREATE INDEX fk_alf_nasp_qn ON alf_node_aspects (qname_id);

CREATE TABLE alf_node_assoc
(
    id INT8 NOT NULL,
    version INT8 NOT NULL,
    source_node_id INT8 NOT NULL,
    target_node_id INT8 NOT NULL,
    type_qname_id INT8 NOT NULL,
    PRIMARY KEY (id),    
    CONSTRAINT fk_alf_nass_snode FOREIGN KEY (source_node_id) REFERENCES alf_node (id),
    CONSTRAINT fk_alf_nass_tnode FOREIGN KEY (target_node_id) REFERENCES alf_node (id),
    CONSTRAINT fk_alf_nass_tqn FOREIGN KEY (type_qname_id) REFERENCES alf_qname (id)
);
CREATE UNIQUE INDEX source_node_id ON alf_node_assoc (source_node_id, target_node_id, type_qname_id);
CREATE INDEX k_alf_nass_snode ON alf_node_assoc (source_node_id);
CREATE INDEX fk_alf_nass_tnode ON alf_node_assoc (target_node_id);
CREATE INDEX fk_alf_nass_tqn ON alf_node_assoc (type_qname_id);
CREATE SEQUENCE alf_node_assoc_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE alf_node_properties
(
    node_id INT8 NOT NULL,
    actual_type_n INT4 NOT NULL,
    persisted_type_n INT4 NOT NULL,
    boolean_value BOOL,
    long_value INT8,
    float_value FLOAT4,
    double_value FLOAT8,
    string_value VARCHAR(1024),
    serializable_value BYTEA,
    qname_id INT8 NOT NULL,
    list_index INT4 NOT NULL,
    locale_id INT8 NOT NULL,
    PRIMARY KEY (node_id, qname_id, list_index, locale_id),
    CONSTRAINT fk_alf_nprop_loc FOREIGN KEY (locale_id) REFERENCES alf_locale (id),
    CONSTRAINT fk_alf_nprop_n FOREIGN KEY (node_id) REFERENCES alf_node (id),
    CONSTRAINT fk_alf_nprop_qn FOREIGN KEY (qname_id) REFERENCES alf_qname (id)
);
CREATE INDEX fk_alf_nprop_n ON alf_node_properties (node_id);
CREATE INDEX fk_alf_nprop_qn ON alf_node_properties (qname_id);
CREATE INDEX fk_alf_nprop_loc ON alf_node_properties (locale_id);

CREATE TABLE alf_usage_delta
(
    id INT8 NOT NULL,
    version INT8 NOT NULL,
    node_id INT8 NOT NULL,
    delta_size INT8 NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_alf_usaged_n FOREIGN KEY (node_id) REFERENCES alf_node (id)
);
CREATE INDEX fk_alf_usaged_n ON alf_usage_delta (node_id);
CREATE SEQUENCE alf_usage_delta_seq START WITH 1 INCREMENT BY 1;
