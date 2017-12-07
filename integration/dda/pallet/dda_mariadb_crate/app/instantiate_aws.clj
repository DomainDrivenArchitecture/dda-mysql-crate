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
(ns dda.pallet.dda-mariadb-crate.app.instantiate-aws
  (:require
    [pallet.repl :as pr]
    [clojure.inspector :as inspector]
    [dda.config.commons.map-utils :as mu]
    [dda.pallet.commons.encrypted-credentials :as crypto]
    [dda.pallet.commons.session-tools :as session-tools]
    [dda.pallet.commons.pallet-schema :as ps]
    [dda.pallet.commons.operation :as operation]
    [dda.pallet.commons.aws :as cloud-target]
    [dda.pallet.dda-mariadb-crate.app :as app]))

(def domain-config
  {:db [{:db-user-passwd "xxxx",
         :db-name "db",
         :db-user-name "user",}]
   :settings #{},
   :root-passwd "xxxx",})

(defn provisioning-spec [count]
  (merge
    (app/mariadb-group-spec (app/app-configuration domain-config))
    (cloud-target/node-spec "jem")
    {:count count}))

(defn converge-install
  [count & options]
  (let [{:keys [gpg-key-id gpg-passphrase
                summarize-session]
         :or {summarize-session true}} options]
   (operation/do-converge-install
     (if (some? gpg-key-id)
       (cloud-target/provider gpg-key-id gpg-passphrase)
       (cloud-target/provider))
     (provisioning-spec count)
     :summarize-session summarize-session)))

(defn configure
 [& options]
 (let [{:keys [gpg-key-id gpg-passphrase
               summarize-session]
        :or {summarize-session true}} options]
  (operation/do-apply-configure
    (if (some? gpg-key-id)
      (cloud-target/provider gpg-key-id gpg-passphrase)
      (cloud-target/provider))
    (provisioning-spec 0)
    :summarize-session summarize-session)))

(defn serverspec
  [& options]
  (let [{:keys [gpg-key-id gpg-passphrase
                summarize-session]
         :or {summarize-session true}} options]
   (operation/do-server-test
     (if (some? gpg-key-id)
       (cloud-target/provider gpg-key-id gpg-passphrase)
       (cloud-target/provider))
     (provisioning-spec 0)
     :summarize-session summarize-session)))
