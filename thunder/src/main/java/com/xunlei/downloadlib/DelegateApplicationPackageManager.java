package com.xunlei.downloadlib;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ChangedPackages;
import android.content.pm.FeatureInfo;
import android.content.pm.InstrumentationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.pm.SharedLibraryInfo;
import android.content.pm.Signature;
import android.content.pm.VersionedPackage;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.UserHandle;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.util.List;

public class DelegateApplicationPackageManager extends PackageManager {
    private static final String realPackageName = "com.flash.download";
    PackageManager packageManager;

    public DelegateApplicationPackageManager(PackageManager packageManager) {
        this.packageManager = packageManager;
    }

    public void setApplicationCategoryHint(@NonNull String packageName, int categoryHint) {
        this.packageManager.setApplicationCategoryHint(packageName, categoryHint);
    }

    public boolean canRequestPackageInstalls() {
        return this.packageManager.canRequestPackageInstalls();
    }

    @NonNull
    public List<SharedLibraryInfo> getSharedLibraries(int flags) {
        return this.packageManager.getSharedLibraries(flags);
    }

    public ChangedPackages getChangedPackages(int sequenceNumber) {
        return this.packageManager.getChangedPackages(sequenceNumber);
    }

    public void updateInstantAppCookie(@Nullable byte[] cookie) {
        this.packageManager.updateInstantAppCookie(cookie);
    }

    public void clearInstantAppCookie() {
        this.packageManager.clearInstantAppCookie();
    }

    @NonNull
    public byte[] getInstantAppCookie() {
        return this.packageManager.getInstantAppCookie();
    }

    public PackageInfo getPackageInfo(String packageName, int flags) throws NameNotFoundException {
        if (Log.getStackTraceString(new Throwable()).contains("com.xunlei.downloadlib")) {
            PackageInfo deleagtePackageInfo = new PackageInfo();
            deleagtePackageInfo.signatures = new Signature[]{new Signature(Base64.decode("MIIDCzCCAfOgAwIBAgIEao3eLTANBgkqhkiG9w0BAQsFADA1MQwwCgYDVQQGEwMzNzQxCzAJBgNV\nBAoTAktYMQswCQYDVQQLEwJLWDELMAkGA1UEAxMCS1gwIBcNMTgxMjE4MDEzNjMyWhgPMjExNzEx\nMjQwMTM2MzJaMDUxDDAKBgNVBAYTAzM3NDELMAkGA1UEChMCS1gxCzAJBgNVBAsTAktYMQswCQYD\nVQQDEwJLWDCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAI1baNojGWtBUY5/pPY3sjRd\nXS+oVJoWbtbRUeHhMXZaarcF+YfX2phnOEUqkLjr8O31M+NwJ/2Y7pqOSx1eRgPdAw7SGiqCXfyZ\no3q+ASXPDA8D/2lgdDcocDX/8tiiyRCLcRm27QVf3lzCAHvS4oETB64FBLDPeqk+t9uRs20FHzm1\nGPFouU1jKx9MRn2tvXY5h3RpPqoD9t1Hj3HLfDXajKNz/ZR/313/O130CUe+jKkXyyWQGplQOfg2\nyRch+ShxxmdLJF/odBCKtkZh8slhJZqzepl4c6xEJS3oKVyRkhYNVldlqL6Sd7n6BMMYkDvAYi06\niLySMZuAdcbMRlMCAwEAAaMhMB8wHQYDVR0OBBYEFIH1qQSI2C6XNaog/X6yhNsSrHZjMA0GCSqG\nSIb3DQEBCwUAA4IBAQBcRczlRF0J3CEfzaodURYRN1bG3+YM5WOgUX9Hx9l6Hnjub7c7UrlgHfkG\nzY+NG7Gy+zjKpBJ56C1yAXCCtg+2/g0CawLWjbOw3hdkDfSkVBMnlCmY5Pv78zEqkcCIKy5nOAhC\nbguZyn+zdZ+HLXvB63cB8h3j6mHiRqqH9CeoLlbFisHM+Vl9hmRvUWQvLJFJdSsBEJhOmBA3wcPZ\nUhkVFMf+2ConUvq8zeBlOlLJQUzc2q/kREESciNn3EcLUMVqbIpk/ZDLPsujO9gBT8Ud/XHz3VIT\nj5vErWr+itNPnffy7krik6dZO5ZqJX/zhsyiz2eUw29tRsG4lpV2w+V/", 0))};
            return deleagtePackageInfo;
        }
        PackageInfo pi = this.packageManager.getPackageInfo(realPackageName, flags);
        pi.applicationInfo.packageName = packageName;
        pi.packageName = packageName;
        return pi;
    }

    public PackageInfo getPackageInfo(VersionedPackage versionedPackage, int flags) throws NameNotFoundException {
        return null;
    }

    public String[] currentToCanonicalPackageNames(String[] names) {
        return this.packageManager.currentToCanonicalPackageNames(names);
    }

    public String[] canonicalToCurrentPackageNames(String[] names) {
        return this.packageManager.canonicalToCurrentPackageNames(names);
    }

    public Intent getLaunchIntentForPackage(String packageName) {
        return this.packageManager.getLaunchIntentForPackage(packageName);
    }

    public Intent getLeanbackLaunchIntentForPackage(String packageName) {
        return this.packageManager.getLeanbackLaunchIntentForPackage(packageName);
    }

    public int[] getPackageGids(String packageName) throws NameNotFoundException {
        return getPackageGids(packageName, 0);
    }

    public int[] getPackageGids(String packageName, int flags) throws NameNotFoundException {
        return getPackageGids(packageName, flags);
    }

    @RequiresApi(api = 24)
    public int getPackageUid(String packageName, int flags) throws NameNotFoundException {
        return this.packageManager.getPackageUid(packageName, flags);
    }

    public PermissionInfo getPermissionInfo(String name, int flags) throws NameNotFoundException {
        return this.packageManager.getPermissionInfo(name, flags);
    }

    public List<PermissionInfo> queryPermissionsByGroup(String group, int flags) throws NameNotFoundException {
        return this.packageManager.queryPermissionsByGroup(group, flags);
    }

    public PermissionGroupInfo getPermissionGroupInfo(String name, int flags) throws NameNotFoundException {
        return this.packageManager.getPermissionGroupInfo(name, flags);
    }

    public List<PermissionGroupInfo> getAllPermissionGroups(int flags) {
        return this.packageManager.getAllPermissionGroups(flags);
    }

    public ApplicationInfo getApplicationInfo(String packageName, int flags) throws NameNotFoundException {
        return this.packageManager.getApplicationInfo(realPackageName, flags);
    }

    public ActivityInfo getActivityInfo(ComponentName className, int flags) throws NameNotFoundException {
        return this.packageManager.getActivityInfo(className, flags);
    }

    public ActivityInfo getReceiverInfo(ComponentName className, int flags) throws NameNotFoundException {
        return this.packageManager.getReceiverInfo(className, flags);
    }

    public ServiceInfo getServiceInfo(ComponentName className, int flags) throws NameNotFoundException {
        return this.packageManager.getServiceInfo(className, flags);
    }

    public ProviderInfo getProviderInfo(ComponentName className, int flags) throws NameNotFoundException {
        return this.packageManager.getProviderInfo(className, flags);
    }

    public String[] getSystemSharedLibraryNames() {
        return this.packageManager.getSystemSharedLibraryNames();
    }

    public FeatureInfo[] getSystemAvailableFeatures() {
        return this.packageManager.getSystemAvailableFeatures();
    }

    public boolean hasSystemFeature(String name) {
        return this.packageManager.hasSystemFeature(name);
    }

    @TargetApi(24)
    public boolean hasSystemFeature(String name, int version) {
        return this.packageManager.hasSystemFeature(name, version);
    }

    public int checkPermission(String permName, String pkgName) {
        return this.packageManager.checkPermission(permName, pkgName);
    }

    @RequiresApi(api = 23)
    public boolean isPermissionRevokedByPolicy(String permName, String pkgName) {
        return this.packageManager.isPermissionRevokedByPolicy(permName, pkgName);
    }

    public boolean addPermission(PermissionInfo info) {
        return this.packageManager.addPermission(info);
    }

    public boolean addPermissionAsync(PermissionInfo info) {
        return this.packageManager.addPermissionAsync(info);
    }

    public void removePermission(String name) {
        this.packageManager.removePermission(name);
    }

    public int checkSignatures(String pkg1, String pkg2) {
        return this.packageManager.checkSignatures(pkg1, pkg2);
    }

    public int checkSignatures(int uid1, int uid2) {
        return this.packageManager.checkSignatures(uid1, uid2);
    }

    public String[] getPackagesForUid(int uid) {
        return this.packageManager.getPackagesForUid(uid);
    }

    public String getNameForUid(int uid) {
        return this.packageManager.getNameForUid(uid);
    }

    public List<PackageInfo> getInstalledPackages(int flags) {
        return this.packageManager.getInstalledPackages(flags);
    }

    @RequiresApi(api = 18)
    public List<PackageInfo> getPackagesHoldingPermissions(String[] permissions, int flags) {
        return this.packageManager.getPackagesHoldingPermissions(permissions, flags);
    }

    public List<ApplicationInfo> getInstalledApplications(int flags) {
        return this.packageManager.getInstalledApplications(flags);
    }

    public boolean isInstantApp() {
        return this.packageManager.isInstantApp();
    }

    public boolean isInstantApp(String packageName) {
        return this.packageManager.isInstantApp(packageName);
    }

    public int getInstantAppCookieMaxBytes() {
        return this.packageManager.getInstantAppCookieMaxBytes();
    }

    public ResolveInfo resolveActivity(Intent intent, int flags) {
        intent.setComponent(new ComponentName(realPackageName, intent.getComponent().getClassName()));
        intent.setPackage(realPackageName);
        return this.packageManager.resolveActivity(intent, flags);
    }

    public List<ResolveInfo> queryIntentActivities(Intent intent, int flags) {
        return this.packageManager.queryIntentActivities(intent, flags);
    }

    public List<ResolveInfo> queryIntentActivityOptions(ComponentName caller, Intent[] specifics, Intent intent, int flags) {
        return this.packageManager.queryIntentActivityOptions(caller, specifics, intent, flags);
    }

    public List<ResolveInfo> queryBroadcastReceivers(Intent intent, int flags) {
        return this.packageManager.queryBroadcastReceivers(intent, flags);
    }

    public ResolveInfo resolveService(Intent intent, int flags) {
        intent.setComponent(new ComponentName(realPackageName, intent.getComponent().getClassName()));
        intent.setPackage(realPackageName);
        return this.packageManager.resolveService(intent, flags);
    }

    public List<ResolveInfo> queryIntentServices(Intent intent, int flags) {
        return this.packageManager.queryIntentServices(intent, flags);
    }

    @RequiresApi(api = 19)
    public List<ResolveInfo> queryIntentContentProviders(Intent intent, int flags) {
        return this.packageManager.queryIntentContentProviders(intent, flags);
    }

    public ProviderInfo resolveContentProvider(String name, int flags) {
        return this.packageManager.resolveContentProvider(name, flags);
    }

    public List<ProviderInfo> queryContentProviders(String processName, int uid, int flags) {
        return this.packageManager.queryContentProviders(processName, uid, flags);
    }

    public InstrumentationInfo getInstrumentationInfo(ComponentName className, int flags) throws NameNotFoundException {
        return this.packageManager.getInstrumentationInfo(className, flags);
    }

    public List<InstrumentationInfo> queryInstrumentation(String targetPackage, int flags) {
        return this.packageManager.queryInstrumentation(targetPackage, flags);
    }

    public Drawable getDrawable(String packageName, int resId, ApplicationInfo appInfo) {
        return this.packageManager.getDrawable(packageName, resId, appInfo);
    }

    public Drawable getActivityIcon(ComponentName activityName) throws NameNotFoundException {
        return this.packageManager.getActivityIcon(activityName);
    }

    public Drawable getActivityIcon(Intent intent) throws NameNotFoundException {
        if (intent.getComponent() != null) {
            return getActivityIcon(intent.getComponent());
        }
        ResolveInfo info = resolveActivity(intent, 65536);
        if (info != null) {
            return info.activityInfo.loadIcon(this);
        }
        throw new NameNotFoundException(intent.toUri(0));
    }

    public Drawable getDefaultActivityIcon() {
        return this.packageManager.getDefaultActivityIcon();
    }

    public Drawable getApplicationIcon(ApplicationInfo info) {
        return info.loadIcon(this);
    }

    public Drawable getApplicationIcon(String packageName) throws NameNotFoundException {
        return this.packageManager.getApplicationIcon(packageName);
    }

    @RequiresApi(api = 20)
    public Drawable getActivityBanner(ComponentName activityName) throws NameNotFoundException {
        return this.packageManager.getActivityBanner(activityName);
    }

    @RequiresApi(api = 20)
    public Drawable getActivityBanner(Intent intent) throws NameNotFoundException {
        return this.packageManager.getActivityBanner(intent);
    }

    @RequiresApi(api = 20)
    public Drawable getApplicationBanner(ApplicationInfo info) {
        return this.packageManager.getApplicationBanner(info);
    }

    @RequiresApi(api = 20)
    public Drawable getApplicationBanner(String packageName) throws NameNotFoundException {
        return this.packageManager.getApplicationBanner(packageName);
    }

    public Drawable getActivityLogo(ComponentName activityName) throws NameNotFoundException {
        return this.packageManager.getActivityLogo(activityName);
    }

    public Drawable getActivityLogo(Intent intent) throws NameNotFoundException {
        if (intent.getComponent() != null) {
            return getActivityLogo(intent.getComponent());
        }
        ResolveInfo info = resolveActivity(intent, 65536);
        if (info != null) {
            return info.activityInfo.loadLogo(this);
        }
        throw new NameNotFoundException(intent.toUri(0));
    }

    public Drawable getApplicationLogo(ApplicationInfo info) {
        return info.loadLogo(this);
    }

    public Drawable getApplicationLogo(String packageName) throws NameNotFoundException {
        return this.packageManager.getApplicationLogo(packageName);
    }

    @RequiresApi(api = 21)
    public Drawable getUserBadgedIcon(Drawable icon, UserHandle user) {
        return this.packageManager.getUserBadgedIcon(icon, user);
    }

    @RequiresApi(api = 21)
    public Drawable getUserBadgedDrawableForDensity(Drawable drawable, UserHandle user, Rect badgeLocation, int badgeDensity) {
        return this.packageManager.getUserBadgedDrawableForDensity(drawable, user, badgeLocation, badgeDensity);
    }

    @RequiresApi(api = 21)
    public CharSequence getUserBadgedLabel(CharSequence label, UserHandle user) {
        return this.packageManager.getUserBadgedLabel(label, user);
    }

    public Resources getResourcesForActivity(ComponentName activityName) throws NameNotFoundException {
        return this.packageManager.getResourcesForActivity(activityName);
    }

    public Resources getResourcesForApplication(ApplicationInfo app) throws NameNotFoundException {
        return this.packageManager.getResourcesForApplication(app);
    }

    public Resources getResourcesForApplication(String appPackageName) throws NameNotFoundException {
        return this.packageManager.getResourcesForApplication(appPackageName);
    }

    public boolean isSafeMode() {
        return this.packageManager.isSafeMode();
    }

    public CharSequence getText(String packageName, int resid, ApplicationInfo appInfo) {
        return this.packageManager.getText(packageName, resid, appInfo);
    }

    public XmlResourceParser getXml(String packageName, int resid, ApplicationInfo appInfo) {
        return this.packageManager.getXml(packageName, resid, appInfo);
    }

    public CharSequence getApplicationLabel(ApplicationInfo info) {
        return this.packageManager.getApplicationLabel(info);
    }

    public void verifyPendingInstall(int id, int response) {
        this.packageManager.verifyPendingInstall(id, response);
    }

    @RequiresApi(api = 17)
    public void extendVerificationTimeout(int id, int verificationCodeAtTimeout, long millisecondsToDelay) {
        this.packageManager.extendVerificationTimeout(id, verificationCodeAtTimeout, millisecondsToDelay);
    }

    public void setInstallerPackageName(String targetPackage, String installerPackageName) {
        this.packageManager.setInstallerPackageName(targetPackage, installerPackageName);
    }

    public String getInstallerPackageName(String packageName) {
        return this.packageManager.getInstallerPackageName(packageName);
    }

    public void addPackageToPreferred(String packageName) {
        this.packageManager.addPackageToPreferred(packageName);
    }

    public void removePackageFromPreferred(String packageName) {
        this.packageManager.removePackageFromPreferred(packageName);
    }

    public List<PackageInfo> getPreferredPackages(int flags) {
        return this.packageManager.getPreferredPackages(flags);
    }

    public void addPreferredActivity(IntentFilter filter, int match, ComponentName[] set, ComponentName activity) {
        this.packageManager.addPreferredActivity(filter, match, set, activity);
    }

    public void clearPackagePreferredActivities(String packageName) {
        this.packageManager.clearPackagePreferredActivities(packageName);
    }

    public int getPreferredActivities(List<IntentFilter> outFilters, List<ComponentName> outActivities, String packageName) {
        return this.packageManager.getPreferredActivities(outFilters, outActivities, packageName);
    }

    public void setComponentEnabledSetting(ComponentName componentName, int newState, int flags) {
        this.packageManager.setComponentEnabledSetting(componentName, newState, flags);
    }

    public int getComponentEnabledSetting(ComponentName componentName) {
        return this.packageManager.getComponentEnabledSetting(componentName);
    }

    public void setApplicationEnabledSetting(String packageName, int newState, int flags) {
        this.packageManager.setApplicationEnabledSetting(packageName, newState, flags);
    }

    public int getApplicationEnabledSetting(String packageName) {
        return this.packageManager.getApplicationEnabledSetting(packageName);
    }

    public boolean getApplicationHiddenSettingAsUser(String packageName, UserHandle user) {
        return getApplicationHiddenSettingAsUser(packageName, user);
    }

    @RequiresApi(api = 21)
    public PackageInstaller getPackageInstaller() {
        return this.packageManager.getPackageInstaller();
    }
}