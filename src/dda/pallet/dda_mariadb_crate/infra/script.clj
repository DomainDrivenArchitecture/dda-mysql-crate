; Licensed to the Apache Software Foundation (ASF) under one
; or more contributor license agreements. See the NOTICE file
; distributed with this work for additional information
; regarding copyright ownership. The ASF licenses this file
; to you under the Apache License, Version 2.0 (the
; "License"); you may not use this file except in compliance
; with the License. You may obtain a copy of the License at
;
; http://www.apache.org/licenses/LICENSE-2.0
;
; Unless required by applicable law or agreed to in writing, software
; distributed under the License is distributed on an "AS IS" BASIS,
; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
; See the License for the specific language governing permissions and
; limitations under the License.

(ns dda.pallet.dda-mariadb-crate.infra.script
  (:require
    [schema.core :as s :include-macros true]
    [clojure.string :as string]
    [pallet.stevedore :as stevedore]
    [pallet.actions :as actions]
    [pallet.api :as api]
    [pallet.crate :as crate]))

(defn- mysql-script*
  [db-user-name db-passwd sql-script]
  (stevedore/script
   ("{\n" mysql "-u" ~db-user-name ~(str "--password=" db-passwd)
    ~(str "<<EOF\n" (string/replace sql-script "`" "\\`") "\nEOF\n}"))))

(defn mysql-script
  "Execute a mysql script. If no user is given it defaults to root."
  [& {:keys [db-user-name
             db-passwd
             sql-script]
      :or {db-user-name "root"
           db-passwd "test1234"}}]
  (actions/exec-checked-script
    "MYSQL command"
    ~(mysql-script* db-user-name db-passwd sql-script)))

(defn create-database
  [& {:keys [db-user-name
             db-passwd
             db-name
             create-options]
      :or {db-user-name "root"
           db-passwd "test1234"
           create-options ""}}]
  (mysql-script
    :db-user-name db-user-name
    :db-passwd db-passwd
    :sql-script (format "CREATE DATABASE IF NOT EXISTS `%s` %s" db-name create-options)))

(defn create-user
  [& {:keys [db-root-user-name
             db-root-passwd
             db-user
             db-passwd]
      :or {db-root-user-name "root"
           db-root-passwd "test1234"}}]
  (mysql-script
    :db-user-name db-root-user-name
    :db-passwd db-root-passwd
    :sql-script
    (format "CREATE USER '%s'@'localhost' IDENTIFIED BY '%s'" db-user db-passwd)))

(defn grant
  "examples for level are `database`.*. \n
users are defined at localhost."
  [& {:keys [db-root-user-name
             db-root-passwd
             db-user-name
             db-user-passwd
             grant-privileges
             grant-level]
      :or {db-root-user-name "root"
           db-root-passwd "test1234"
           grant-privileges "ALL"}}]
  (if db-user-passwd
    (mysql-script
      :db-user-name db-root-user-name
      :db-passwd db-root-passwd
      :sql-script
      (format "GRANT %s ON %s TO '%s'@'localhost' identified by '%s'"
              grant-privileges grant-level db-user-name db-user-passwd))
    (mysql-script
      :db-user-name db-root-user-name
      :db-passwd db-root-passwd
      :sql-script
      (format "GRANT %s ON %s TO %s" grant-privileges grant-level db-user-name))))