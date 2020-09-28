// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.monitor.implementation;

import com.azure.resourcemanager.monitor.MonitorManager;
import com.azure.resourcemanager.monitor.models.ActionGroup;
import com.azure.resourcemanager.monitor.models.AutomationRunbookReceiver;
import com.azure.resourcemanager.monitor.models.AzureAppPushReceiver;
import com.azure.resourcemanager.monitor.models.AzureFunctionReceiver;
import com.azure.resourcemanager.monitor.models.EmailReceiver;
import com.azure.resourcemanager.monitor.models.ItsmReceiver;
import com.azure.resourcemanager.monitor.models.LogicAppReceiver;
import com.azure.resourcemanager.monitor.models.SmsReceiver;
import com.azure.resourcemanager.monitor.models.VoiceReceiver;
import com.azure.resourcemanager.monitor.models.WebhookReceiver;
import com.azure.resourcemanager.monitor.fluent.models.ActionGroupResourceInner;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import reactor.core.publisher.Mono;

/** Implementation for ActionGroup. */
class ActionGroupImpl
    extends GroupableResourceImpl<ActionGroup, ActionGroupResourceInner, ActionGroupImpl, MonitorManager>
    implements ActionGroup,
        ActionGroup.Definition<ActionGroupImpl>,
        ActionGroup.Update,
        ActionGroup.UpdateStages.WithActionUpdateDefinition {
    private static final String EMAIL_SUFFIX = "_-EmailAction-";
    private static final String SMS_SUFFIX = "_-SMSAction-";
    private static final String APP_ACTION_SUFFIX = "_-AzureAppAction-";
    private static final String VOICE_SUFFIX = "_-VoiceAction-";
    private static final String RUN_BOOK_SUFFIX = " (RB)";
    private static final String LOGIC_SUFFIX = " (LA)";
    private static final String FUNCTION_SUFFIX = " (F)";
    private static final String WEBHOOK_SUFFIX = " (WH)";
    private static final String ITSM_SUFFIX = " (ITSM)";

    private String actionReceiverPrefix;
    private TreeMap<String, EmailReceiver> emailReceivers;
    private TreeMap<String, SmsReceiver> smsReceivers;
    private TreeMap<String, AzureAppPushReceiver> appActionReceivers;
    private TreeMap<String, VoiceReceiver> voiceReceivers;
    private TreeMap<String, AutomationRunbookReceiver> runBookReceivers;
    private TreeMap<String, LogicAppReceiver> logicReceivers;
    private TreeMap<String, AzureFunctionReceiver> functionReceivers;
    private TreeMap<String, WebhookReceiver> webhookReceivers;
    private TreeMap<String, ItsmReceiver> itsmReceivers;

    ActionGroupImpl(String name, final ActionGroupResourceInner innerModel, final MonitorManager monitorManager) {
        super(name, innerModel, monitorManager);
        this.actionReceiverPrefix = "";
        this.emailReceivers = new TreeMap<>();
        this.smsReceivers = new TreeMap<>();
        this.appActionReceivers = new TreeMap<>();
        this.voiceReceivers = new TreeMap<>();
        this.runBookReceivers = new TreeMap<>();
        this.logicReceivers = new TreeMap<>();
        this.functionReceivers = new TreeMap<>();
        this.webhookReceivers = new TreeMap<>();
        this.itsmReceivers = new TreeMap<>();
        if (isInCreateMode()) {
            this.innerModel().withEnabled(true);
            this
                .innerModel()
                .withGroupShortName(this.name().substring(0, (this.name().length() > 12) ? 12 : this.name().length()));
        } else {
            this.withExistingResourceGroup(ResourceUtils.groupFromResourceId(this.id()));
        }
    }

    @Override
    public String shortName() {
        return this.innerModel().groupShortName();
    }

    @Override
    public List<EmailReceiver> emailReceivers() {
        return this.innerModel().emailReceivers();
    }

    @Override
    public List<SmsReceiver> smsReceivers() {
        return this.innerModel().smsReceivers();
    }

    @Override
    public List<WebhookReceiver> webhookReceivers() {
        return this.innerModel().webhookReceivers();
    }

    @Override
    public List<ItsmReceiver> itsmReceivers() {
        return this.innerModel().itsmReceivers();
    }

    @Override
    public List<AzureAppPushReceiver> pushNotificationReceivers() {
        return this.innerModel().azureAppPushReceivers();
    }

    @Override
    public List<AutomationRunbookReceiver> automationRunbookReceivers() {
        return this.innerModel().automationRunbookReceivers();
    }

    @Override
    public List<VoiceReceiver> voiceReceivers() {
        return this.innerModel().voiceReceivers();
    }

    @Override
    public List<LogicAppReceiver> logicAppReceivers() {
        return this.innerModel().logicAppReceivers();
    }

    @Override
    public List<AzureFunctionReceiver> azureFunctionReceivers() {
        return this.innerModel().azureFunctionReceivers();
    }

    @Override
    public ActionGroupImpl withoutReceiver(String actionNamePrefix) {
        this.updateReceiver(actionNamePrefix);
        this.withoutEmail();
        this.withoutSms();
        this.withoutPushNotification();
        this.withoutVoice();
        this.withoutAutomationRunbook();
        this.withoutLogicApp();
        this.withoutAzureFunction();
        this.withoutWebhook();
        this.withoutItsm();

        return this.parent();
    }

    @Override
    public ActionGroupImpl defineReceiver(String actionNamePrefix) {
        return this.updateReceiver(actionNamePrefix);
    }

    @Override
    public ActionGroupImpl updateReceiver(String actionNamePrefix) {
        this.actionReceiverPrefix = actionNamePrefix;
        this.emailReceivers.clear();
        this.smsReceivers.clear();
        this.appActionReceivers.clear();
        this.voiceReceivers.clear();
        this.runBookReceivers.clear();
        this.logicReceivers.clear();
        this.functionReceivers.clear();
        this.webhookReceivers.clear();
        this.itsmReceivers.clear();

        if (this.innerModel().emailReceivers() != null) {
            for (EmailReceiver er : this.innerModel().emailReceivers()) {
                this.emailReceivers.put(er.name(), er);
            }
        }

        if (this.innerModel().smsReceivers() != null) {
            for (SmsReceiver sr : this.innerModel().smsReceivers()) {
                this.smsReceivers.put(sr.name(), sr);
            }
        }

        if (this.innerModel().azureAppPushReceivers() != null) {
            for (AzureAppPushReceiver ar : this.innerModel().azureAppPushReceivers()) {
                this.appActionReceivers.put(ar.name(), ar);
            }
        }

        if (this.innerModel().voiceReceivers() != null) {
            for (VoiceReceiver vr : this.innerModel().voiceReceivers()) {
                this.voiceReceivers.put(vr.name(), vr);
            }
        }

        if (this.innerModel().automationRunbookReceivers() != null) {
            for (AutomationRunbookReceiver ar : this.innerModel().automationRunbookReceivers()) {
                this.runBookReceivers.put(ar.name(), ar);
            }
        }

        if (this.innerModel().logicAppReceivers() != null) {
            for (LogicAppReceiver lr : this.innerModel().logicAppReceivers()) {
                this.logicReceivers.put(lr.name(), lr);
            }
        }

        if (this.innerModel().azureFunctionReceivers() != null) {
            for (AzureFunctionReceiver fr : this.innerModel().azureFunctionReceivers()) {
                this.functionReceivers.put(fr.name(), fr);
            }
        }

        if (this.innerModel().webhookReceivers() != null) {
            for (WebhookReceiver wr : this.innerModel().webhookReceivers()) {
                this.webhookReceivers.put(wr.name(), wr);
            }
        }

        if (this.innerModel().itsmReceivers() != null) {
            for (ItsmReceiver ir : this.innerModel().itsmReceivers()) {
                this.itsmReceivers.put(ir.name(), ir);
            }
        }
        return this;
    }

    @Override
    public ActionGroupImpl withShortName(String shortName) {
        this.innerModel().withGroupShortName(shortName);
        return this;
    }

    @Override
    public Mono<ActionGroup> createResourceAsync() {
        this.innerModel().withLocation("global");
        return this
            .manager()
            .serviceClient()
            .getActionGroups()
            .createOrUpdateAsync(this.resourceGroupName(), this.name(), this.innerModel())
            .map(innerToFluentMap(this));
    }

    @Override
    protected Mono<ActionGroupResourceInner> getInnerAsync() {
        return this.manager().serviceClient().getActionGroups()
            .getByResourceGroupAsync(this.resourceGroupName(), this.name());
    }

    @Override
    public ActionGroupImpl withEmail(String emailAddress) {
        this.withoutEmail();

        String compositeKey = this.actionReceiverPrefix + EMAIL_SUFFIX;
        EmailReceiver er = new EmailReceiver();
        er.withName(compositeKey);
        er.withEmailAddress(emailAddress);

        this.emailReceivers.put(compositeKey, er);
        return this;
    }

    @Override
    public ActionGroupImpl withSms(String countryCode, String phoneNumber) {
        this.withoutSms();

        String compositeKey = this.actionReceiverPrefix + SMS_SUFFIX;
        SmsReceiver sr = new SmsReceiver();
        sr.withName(compositeKey);
        sr.withCountryCode(countryCode);
        sr.withPhoneNumber(phoneNumber);

        this.smsReceivers.put(compositeKey, sr);
        return this;
    }

    @Override
    public ActionGroupImpl withWebhook(String serviceUri) {
        this.withoutWebhook();

        String compositeKey = this.actionReceiverPrefix + WEBHOOK_SUFFIX;
        WebhookReceiver wr = new WebhookReceiver();
        wr.withName(compositeKey);
        wr.withServiceUri(serviceUri);

        this.webhookReceivers.put(compositeKey, wr);
        return this;
    }

    @Override
    public ActionGroupImpl withItsm(
        String workspaceId, String connectionId, String ticketConfiguration, String region) {
        this.withoutItsm();

        String compositeKey = this.actionReceiverPrefix + ITSM_SUFFIX;
        ItsmReceiver ir = new ItsmReceiver();
        ir.withName(compositeKey);
        ir.withWorkspaceId(workspaceId);
        ir.withConnectionId(connectionId);
        ir.withRegion(region);
        ir.withTicketConfiguration(ticketConfiguration);

        this.itsmReceivers.put(compositeKey, ir);
        return this;
    }

    @Override
    public ActionGroupImpl withPushNotification(String emailAddress) {
        this.withoutPushNotification();

        String compositeKey = this.actionReceiverPrefix + APP_ACTION_SUFFIX;
        AzureAppPushReceiver ar = new AzureAppPushReceiver();
        ar.withName(compositeKey);
        ar.withEmailAddress(emailAddress);

        this.appActionReceivers.put(compositeKey, ar);
        return this;
    }

    @Override
    public ActionGroupImpl withAutomationRunbook(
        String automationAccountId, String runbookName, String webhookResourceId, boolean isGlobalRunbook) {
        this.withoutAutomationRunbook();

        String compositeKey = this.actionReceiverPrefix + RUN_BOOK_SUFFIX;
        AutomationRunbookReceiver arr = new AutomationRunbookReceiver();
        arr.withName(compositeKey);
        arr.withAutomationAccountId(automationAccountId);
        arr.withRunbookName(runbookName);
        arr.withWebhookResourceId(webhookResourceId);
        arr.withIsGlobalRunbook(isGlobalRunbook);

        this.runBookReceivers.put(compositeKey, arr);
        return this;
    }

    @Override
    public ActionGroupImpl withVoice(String countryCode, String phoneNumber) {
        this.withoutVoice();

        String compositeKey = this.actionReceiverPrefix + VOICE_SUFFIX;
        VoiceReceiver vr = new VoiceReceiver();
        vr.withName(compositeKey);
        vr.withCountryCode(countryCode);
        vr.withPhoneNumber(phoneNumber);

        this.voiceReceivers.put(compositeKey, vr);
        return this;
    }

    @Override
    public ActionGroupImpl withLogicApp(String logicAppResourceId, String callbackUrl) {
        this.withoutLogicApp();

        String compositeKey = this.actionReceiverPrefix + LOGIC_SUFFIX;
        LogicAppReceiver lr = new LogicAppReceiver();
        lr.withName(compositeKey);
        lr.withResourceId(logicAppResourceId);
        lr.withCallbackUrl(callbackUrl);

        this.logicReceivers.put(compositeKey, lr);
        return this;
    }

    @Override
    public ActionGroupImpl withAzureFunction(String functionAppResourceId, String functionName, String httpTriggerUrl) {
        this.withoutAzureFunction();
        String compositeKey = this.actionReceiverPrefix + FUNCTION_SUFFIX;

        AzureFunctionReceiver afr = new AzureFunctionReceiver();
        afr.withName(compositeKey);
        afr.withFunctionAppResourceId(functionAppResourceId);
        afr.withFunctionName(functionName);
        afr.withHttpTriggerUrl(httpTriggerUrl);

        this.functionReceivers.put(compositeKey, afr);
        return this;
    }

    @Override
    public ActionGroupImpl attach() {
        this.actionReceiverPrefix = "";

        populateReceivers();

        this.emailReceivers.clear();
        this.smsReceivers.clear();
        this.appActionReceivers.clear();
        this.voiceReceivers.clear();
        this.runBookReceivers.clear();
        this.logicReceivers.clear();
        this.functionReceivers.clear();
        this.webhookReceivers.clear();
        this.itsmReceivers.clear();
        return this;
    }

    @Override
    public ActionGroupImpl withoutEmail() {
        String compositeKey = this.actionReceiverPrefix + EMAIL_SUFFIX;
        if (this.emailReceivers.containsKey(compositeKey)) {
            this.emailReceivers.remove(compositeKey);
        }
        if (this.emailReceivers.containsKey(this.actionReceiverPrefix)) {
            this.emailReceivers.remove(actionReceiverPrefix);
        }
        return this;
    }

    @Override
    public ActionGroupImpl withoutSms() {
        String compositeKey = this.actionReceiverPrefix + SMS_SUFFIX;
        if (this.smsReceivers.containsKey(compositeKey)) {
            this.smsReceivers.remove(compositeKey);
        }
        if (this.smsReceivers.containsKey(this.actionReceiverPrefix)) {
            this.smsReceivers.remove(actionReceiverPrefix);
        }
        return this;
    }

    @Override
    public ActionGroupImpl withoutWebhook() {
        String compositeKey = this.actionReceiverPrefix + WEBHOOK_SUFFIX;
        if (this.webhookReceivers.containsKey(compositeKey)) {
            this.webhookReceivers.remove(compositeKey);
        }
        if (this.webhookReceivers.containsKey(this.actionReceiverPrefix)) {
            this.webhookReceivers.remove(actionReceiverPrefix);
        }
        return this;
    }

    @Override
    public ActionGroupImpl withoutItsm() {
        String compositeKey = this.actionReceiverPrefix + ITSM_SUFFIX;
        if (this.itsmReceivers.containsKey(compositeKey)) {
            this.itsmReceivers.remove(compositeKey);
        }
        if (this.itsmReceivers.containsKey(this.actionReceiverPrefix)) {
            this.itsmReceivers.remove(actionReceiverPrefix);
        }
        return this;
    }

    @Override
    public ActionGroupImpl withoutPushNotification() {
        String compositeKey = this.actionReceiverPrefix + APP_ACTION_SUFFIX;
        if (this.appActionReceivers.containsKey(compositeKey)) {
            this.appActionReceivers.remove(compositeKey);
        }
        if (this.appActionReceivers.containsKey(this.actionReceiverPrefix)) {
            this.appActionReceivers.remove(actionReceiverPrefix);
        }
        return this;
    }

    @Override
    public ActionGroupImpl withoutAutomationRunbook() {
        String compositeKey = this.actionReceiverPrefix + RUN_BOOK_SUFFIX;
        if (this.runBookReceivers.containsKey(compositeKey)) {
            this.runBookReceivers.remove(compositeKey);
        }
        if (this.runBookReceivers.containsKey(this.actionReceiverPrefix)) {
            this.runBookReceivers.remove(actionReceiverPrefix);
        }
        return this;
    }

    @Override
    public ActionGroupImpl withoutVoice() {
        String compositeKey = this.actionReceiverPrefix + VOICE_SUFFIX;
        if (this.voiceReceivers.containsKey(compositeKey)) {
            this.voiceReceivers.remove(compositeKey);
        }
        if (this.voiceReceivers.containsKey(this.actionReceiverPrefix)) {
            this.voiceReceivers.remove(actionReceiverPrefix);
        }
        return this;
    }

    @Override
    public ActionGroupImpl withoutLogicApp() {
        String compositeKey = this.actionReceiverPrefix + LOGIC_SUFFIX;
        if (this.logicReceivers.containsKey(compositeKey)) {
            this.logicReceivers.remove(compositeKey);
        }
        if (this.logicReceivers.containsKey(this.actionReceiverPrefix)) {
            this.logicReceivers.remove(actionReceiverPrefix);
        }
        return this;
    }

    @Override
    public ActionGroupImpl withoutAzureFunction() {
        String compositeKey = this.actionReceiverPrefix + LOGIC_SUFFIX;
        if (this.functionReceivers.containsKey(compositeKey)) {
            this.functionReceivers.remove(compositeKey);
        }
        if (this.functionReceivers.containsKey(this.actionReceiverPrefix)) {
            this.functionReceivers.remove(actionReceiverPrefix);
        }
        return this;
    }

    @Override
    public ActionGroupImpl parent() {
        return this.attach();
    }

    private void populateReceivers() {
        if (this.emailReceivers.values().size() > 0) {
            if (this.innerModel().emailReceivers() == null) {
                this.innerModel().withEmailReceivers(new ArrayList<EmailReceiver>());
            } else {
                this.innerModel().emailReceivers().clear();
            }
            this.innerModel().emailReceivers().addAll(this.emailReceivers.values());
        } else {
            this.innerModel().withEmailReceivers(null);
        }

        if (this.smsReceivers.values().size() > 0) {
            if (this.innerModel().smsReceivers() == null) {
                this.innerModel().withSmsReceivers(new ArrayList<SmsReceiver>());
            } else {
                this.innerModel().smsReceivers().clear();
            }
            this.innerModel().smsReceivers().addAll(this.smsReceivers.values());
        } else {
            this.innerModel().withSmsReceivers(null);
        }

        if (this.appActionReceivers.values().size() > 0) {
            if (this.innerModel().azureAppPushReceivers() == null) {
                this.innerModel().withAzureAppPushReceivers(new ArrayList<AzureAppPushReceiver>());
            } else {
                this.innerModel().azureAppPushReceivers().clear();
            }
            this.innerModel().azureAppPushReceivers().addAll(this.appActionReceivers.values());
        } else {
            this.innerModel().withAzureAppPushReceivers(null);
        }

        if (this.voiceReceivers.values().size() > 0) {
            if (this.innerModel().voiceReceivers() == null) {
                this.innerModel().withVoiceReceivers(new ArrayList<VoiceReceiver>());
            } else {
                this.innerModel().voiceReceivers().clear();
            }
            this.innerModel().voiceReceivers().addAll(this.voiceReceivers.values());
        } else {
            this.innerModel().withVoiceReceivers(null);
        }

        if (this.runBookReceivers.values().size() > 0) {
            if (this.innerModel().automationRunbookReceivers() == null) {
                this.innerModel().withAutomationRunbookReceivers(new ArrayList<AutomationRunbookReceiver>());
            } else {
                this.innerModel().automationRunbookReceivers().clear();
            }
            this.innerModel().automationRunbookReceivers().addAll(this.runBookReceivers.values());
        } else {
            this.innerModel().withAutomationRunbookReceivers(null);
        }

        if (this.logicReceivers.values().size() > 0) {
            if (this.innerModel().logicAppReceivers() == null) {
                this.innerModel().withLogicAppReceivers(new ArrayList<LogicAppReceiver>());
            } else {
                this.innerModel().logicAppReceivers().clear();
            }
            this.innerModel().logicAppReceivers().addAll(this.logicReceivers.values());
        } else {
            this.innerModel().withLogicAppReceivers(null);
        }

        if (this.functionReceivers.values().size() > 0) {
            if (this.innerModel().azureFunctionReceivers() == null) {
                this.innerModel().withAzureFunctionReceivers(new ArrayList<AzureFunctionReceiver>());
            } else {
                this.innerModel().azureFunctionReceivers().clear();
            }
            this.innerModel().azureFunctionReceivers().addAll(this.functionReceivers.values());
        } else {
            this.innerModel().withAzureFunctionReceivers(null);
        }

        if (this.webhookReceivers.values().size() > 0) {
            if (this.innerModel().webhookReceivers() == null) {
                this.innerModel().withWebhookReceivers(new ArrayList<WebhookReceiver>());
            } else {
                this.innerModel().webhookReceivers().clear();
            }
            this.innerModel().webhookReceivers().addAll(this.webhookReceivers.values());
        } else {
            this.innerModel().withWebhookReceivers(null);
        }

        if (this.itsmReceivers.values().size() > 0) {
            if (this.innerModel().itsmReceivers() == null) {
                this.innerModel().withItsmReceivers(new ArrayList<ItsmReceiver>());
            } else {
                this.innerModel().itsmReceivers().clear();
            }
            this.innerModel().itsmReceivers().addAll(this.itsmReceivers.values());
        } else {
            this.innerModel().withItsmReceivers(null);
        }
    }
}
