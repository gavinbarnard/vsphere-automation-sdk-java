/*
 * *******************************************************
 * Copyright VMware, Inc. 2018.  All Rights Reserved.
 * SPDX-License-Identifier: MIT
 * *******************************************************
 *
 * DISCLAIMER. THIS PROGRAM IS PROVIDED TO YOU "AS IS" WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, WHETHER ORAL OR WRITTEN,
 * EXPRESS OR IMPLIED. THE AUTHOR SPECIFICALLY DISCLAIMS ANY IMPLIED
 * WARRANTIES OR CONDITIONS OF MERCHANTABILITY, SATISFACTORY QUALITY,
 * NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE.
 */

package vmware.samples.vmc.networks;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.Option;

import com.vmware.nsx_vmc.client.VmcNsxClients;
import com.vmware.vapi.client.ApiClient;
import com.vmware.nsx_policy.infra.domains.gateway_policies.Rules;
import com.vmware.nsx_policy.model.Rule;

import vmware.samples.common.VmcSamplesAbstractBase;

/*-
 * This example shows how to authenticate to the VMC (VMware Cloud on AWS)
 * service, using a VMC refresh token to obtain an authentication token that
 * can then be used to authenticate to the NSX-T instance in a Software
 * Defined Data Center (SDDC). It also shows how to list several types
 * of entities.
 */
public class NsxTFirewallRulesCrud extends VmcSamplesAbstractBase {

    private String orgId;
    private String sddcId;
    private String ruleName;
    private ApiClient apiClient;

    public static String VMC_SERVER = "https://vmc.vmware.com";
    public static String CSP_SERVER = "https://console.cloud.vmware.com/csp/gateway/am/api/auth/api-tokens/authorize";

    /**
     * Define the options specific to this sample and configure the sample using
     * command-line arguments or a config file
     *
     * @param args command line arguments passed to the sample
     */
    @Override
    protected void parseArgs(final String[] args) {
        final Option ruleNameOption = Option.builder().longOpt("rule_name").desc("Specify the rule to get")
                .argName("RULE NAME").required(true).hasArg().build();
        final Option orgOption = Option.builder().longOpt("org_id").desc("Specify the organization id")
                .argName("ORGANIZATION ID").required(true).hasArg().build();
        final Option sddcOption = Option.builder().longOpt("sddc_id").desc("Specify the SDDC id").argName("SDDC ID")
                .required(true).hasArg().build();
        final List<Option> optionList = Arrays.asList(orgOption, sddcOption, ruleNameOption);

        super.parseArgs(optionList, args);
        this.orgId = (String) parsedOptions.get("org_id");
        this.sddcId = (String) parsedOptions.get("sddc_id");
        this.ruleName = (String) parsedOptions.get("rule_name");
    }

    @Override
    protected void setup() throws Exception {
        // Create the API client. This client will automatically obtain
        // and use an authentication token from the VMC CSP service,
        // and will automatically refresh it when it expires.
        this.apiClient = VmcNsxClients.custom()
            .setBaseUrl(VMC_SERVER)
            .setAuthorizationUrl(CSP_SERVER)
            .setRefreshToken(this.refreshToken.toCharArray())
            .setOrganizationId(this.orgId)
            .setSddcId(this.sddcId)
            .setVerifyServerCertificate(false)
            .setVerifyServerHostname(false)
            .build();
    }

    public void firewallCrudOperation() {
        Rules rulesStub = this.apiClient.createStub(Rules.class);
        System.out.println("Creating a temporary rule named " + this.ruleName + "\n");
        try{
            Rule newRule = new Rule();
            newRule.setAction("ALLOW");
            newRule.setDisplayName("Temporary Rule");
            List<String> ruleScope = Arrays.asList("/infra/labels/cgw-all"); 
            newRule.setScope(ruleScope);
            List<String> ruleServices = Arrays.asList("ANY");
            newRule.setServices(ruleServices);
            newRule.setSourceGroups(ruleServices);
            newRule.setDestinationGroups(ruleServices);
            rulesStub.patch("cgw", "default", this.ruleName, newRule);
        } catch (Error e)
        { 
            System.out.println(e);
        }
        System.out.println("Getting Rule " + this.ruleName + "\n");
        try {
            Rule rule = rulesStub.get("cgw", "default", this.ruleName);
            System.out.println(rule.getDisplayName());
            System.out.println(rule.getScope());
            System.out.println(rule.getSourceGroups());
            System.out.println(rule.getDestinationGroups());
            System.out.println(rule.getServices());
            System.out.println(rule.getAction());
        } catch (Error e)
        {
            System.out.println(e);
        }
        try {
            System.out.println("Sleeping for a few seconds");
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            System.err.format("IOException: %s%n", e);
        }
        System.out.println("Updating Rule " + this.ruleName + "\n");
        try {
            Rule newRule = new Rule();
            newRule.setAction("ALLOW");
            newRule.setDisplayName("Temporary Rule - Updated Name");
            List<String> ruleScope = Arrays.asList("/infra/labels/cgw-all"); 
            newRule.setScope(ruleScope);
            List<String> ruleServices = Arrays.asList("ANY");
            newRule.setServices(ruleServices);
            newRule.setSourceGroups(ruleServices);
            newRule.setDestinationGroups(ruleServices);
            rulesStub.patch("cgw", "default", this.ruleName, newRule);
        } catch (Error e)
        {
            System.out.println(e);
        }
        try {
            System.out.println("Sleeping for a few seconds");
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            System.err.format("IOException: %s%n", e);
        }
        System.out.println("Deleting the rule "+ this.ruleName + "\n");
        try {
            rulesStub.delete("cgw", "default", this.ruleName);
        } catch (Error e)
        {
            System.out.println(e);
        }
    }

    @Override
    protected void run() throws Exception {
        firewallCrudOperation();
    }

    @Override
    protected void cleanup() throws Exception {
    }

    public static void main(final String[] args) throws Exception {
        /*
         * Execute the sample using the command line arguments or parameters
         * from the configuration file. This executes the following steps:
         * 1. Parse the arguments required by the sample
         * 2. Login to the server
         * 3. Setup any resources required by the sample run
         * 4. Run the sample
         * 5. Cleanup any data created by the sample run, if cleanup=true
         * 6. Logout of the server
         */
        new NsxTFirewallRulesCrud().execute(args);
    }
}
