<!-----------------------------------------------------------------------------
 * Copyright (c) 2021 Sierra Wireless and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 *
 * The Eclipse Public License is available at
 *    http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *    http://www.eclipse.org/org/documents/edl-v10.html.
 * Orange Polska S.A. -  optional objects support added
  ----------------------------------------------------------------------------->
<template>
  <v-card class="mb-12" elevation="0">
    <v-card-text class="pb-0">
      <p>
        This information will be used to add
        <strong>Optional Objects</strong> 36050 and 36051 to your LWM2M Client during the
        bootstrap session.
      </p>
    </v-card-text>
    <v-form ref="form" :model-value="valid" @update:model-value="$emit('update:valid', $event)">
      <v-switch v-model="add36050" label="Add Object 36050" @change="updateConfig"></v-switch>
      <v-container v-if="add36050">
        <v-text-field v-model="ConnectionIdentity.ID" label="ID" @input="updateConfig"></v-text-field>
        <v-text-field v-model="ConnectionIdentity.PSKIdentity" label="PSK Identity" @input="updateConfig"></v-text-field>
        <v-text-field v-model="ConnectionIdentity.PSKSecretKey" label="PSK Secret Key" type="password"
        :rules="[rules.required, rules.hex]" hint="Must be a hexadecimal string (e.g., a1b2c3...)" persistent-hint
          @input="updateConfig" />
      </v-container>

      <v-switch v-model="add36051" label="Add Object 36051" @change="updateConfig"></v-switch>
         <v-container v-if="add36051">
           <div v-for="(conn, index) in ConnectionServiceEndpoints" :key="index" class="mb-4">
             <v-text-field v-model="conn.ServiceName" label="Service Name" @input="updateConfig"></v-text-field>
             <v-text-field v-model="conn.Payload" label="Payload" @input="updateConfig"></v-text-field>
             <v-text-field v-model="conn.ServiceURI" label="Service URI" @input="updateConfig"></v-text-field>
             <v-text-field v-model="conn.TopicRoot" label="Topic Root" @input="updateConfig"></v-text-field>
             <v-textarea  v-model="conn.ServerPublicKey" label="Server Public Key" :rules="[rules.optional, rules.base64]"
               hint="Required. Must be in Base64 format."persistent-hint spellcheck="false" rows="2"
             @input="updateConfig"/>
             <v-btn color="error" @click="removeConnection(index)">Remove</v-btn>
           </div>
           <v-btn color="primary" @click="addConnection">Add Another Connection</v-btn>
         </v-container>
    </v-form>
  </v-card>
</template>
<script>
import {
  fromHex,
} from "@leshan-demo-servers-shared/js/byteutils.js";
export default {
  props: {
    valid: Boolean,
  },
data() {
  return {
    add36050: false,
    add36051: false,
    ConnectionServiceEndpoints: [],
    ConnectionIdentity: {
      ID: "",
      PSKIdentity: "",
      PSKSecretKey: "",
    },
        rules: {
          required: value => !!value || 'This field is required.',
          base64: value =>
            !value || /^[A-Za-z0-9+/=]+$/.test(value) || 'Must be valid Base64.',
          hex: value =>
            !value || /^[0-9a-fA-F]+$/.test(value) || 'Must be a valid hexadecimal string.',
        },
  };
},
  watch: {
    add36050(newValue) {
      if (!newValue) {
        this.ConnectionIdentity = {
          ID: "",
          PSKIdentity: "",
          PSKSecretKey: "",
        };
      }
      this.updateConfig();
    },
add36051(newValue) {
  if (!newValue) {
    this.ConnectionServiceEndpoints = [];
  } else if (this.ConnectionServiceEndpoints.length === 0) {
    this.addConnection();
  }
  this.updateConfig();
},
  },
methods: {
  updateConfig() {
const configPayload = {
  add36050: this.add36050,
   ConnectionIdentity: {
        ID: this.ConnectionIdentity.ID,
        PSKIdentity: this.ConnectionIdentity.PSKIdentity,
        PSKSecretKey: fromHex(this.ConnectionIdentity.PSKSecretKey),
      },
  add36051: this.add36051,
  ConnectionServiceEndpoint: this.ConnectionServiceEndpoints,
};
    this.$emit("update:modelValue", configPayload);
    this.$emit("updateConfig", configPayload);
  },
  addConnection() {
    this.ConnectionServiceEndpoints.push({ ServiceName: "", Payload: "", ServiceURI: "", TopicRoot: "", ServerPublicKey: "" });
    this.updateConfig();
  },
  removeConnection(index) {
    this.ConnectionServiceEndpoints.splice(index, 1);
    this.updateConfig();
  },
},
};
</script>
