

package com.siliconstudios.dynamicshopguiplus.methodes;

import com.siliconstudios.dynamicshopguiplus.DynamicShopGUIPlus;
import com.siliconstudios.dynamicshopguiplus.files.Lang;
import com.siliconstudios.dynamicshopguiplus.methodes.SendMessage;

public class UpdateChecker {
    public final Version CURRENT_VERSION = new Version(DynamicShopGUIPlus.getInstance().getDescription().getVersion());
    public Version LATEST_VERSION;
    public boolean updateAvailable;

    public UpdateChecker() {
        this.LATEST_VERSION = this.CURRENT_VERSION;
        this.updateAvailable = false;
    }

    private boolean isUpdateAvailable() {
        if (this.CURRENT_VERSION.isSmaller(this.LATEST_VERSION)) {
            return this.CURRENT_VERSION.isDev() || !this.LATEST_VERSION.isDev();
        }
        return false;
    }

    public void checkForUpdates() {
        if (this.updateAvailable) {
            SendMessage.infoMessage(Lang.UPDATE_AVAILABLE.get().replace("%plugin_version%", this.CURRENT_VERSION.getVer()).replace("%latest_version%", this.LATEST_VERSION.getVer()));
        }
    }

    public static final class Version {
        private final String ver;
        private final boolean dev;
        private int major = 0;
        private int minor = 0;
        private int patch = 0;
        private int beta = 0;

        public Version(String version) {
            String[] parts;
            this.ver = version;
            if (version.matches(".*[a-zA-Z].*")) {
                parts = version.replace("b", "").replace("a", "").replace("r", "").replace("-", "").replace("v", "").split("\\.");
                this.dev = true;
            } else {
                parts = version.split("\\.");
                this.dev = false;
            }
            try {
                this.major = parts.length >= 1 ? Integer.parseInt(parts[0]) : 0;
                this.minor = parts.length >= 2 ? Integer.parseInt(parts[1]) : 0;
                this.patch = parts.length >= 3 ? Integer.parseInt(parts[2]) : 0;
                this.beta = parts.length >= 4 ? Integer.parseInt(parts[3]) : 0;
            }
            catch (NumberFormatException e) {
                DynamicShopGUIPlus.getInstance().getServer().getLogger().warning("Invalid version numbering for '" + version + "'");
            }
        }

        public boolean isGreater(Version ver) {
            return this.major > ver.major || this.major == ver.major && this.minor > ver.minor || this.major == ver.major && this.minor == ver.minor && this.patch > ver.patch || this.major == ver.major && this.minor == ver.minor && this.patch == ver.patch && this.beta > ver.beta;
        }

        public boolean isSmaller(Version ver) {
            return this.major < ver.major || this.major == ver.major && this.minor < ver.minor || this.major == ver.major && this.minor == ver.minor && this.patch < ver.patch || this.major == ver.major && this.minor == ver.minor && this.patch == ver.patch && this.beta < ver.beta;
        }

        public boolean isSame(Version ver) {
            return this.major == ver.major && this.minor == ver.minor && this.patch == ver.patch && this.beta == ver.beta;
        }

        public boolean isDev() {
            return this.dev;
        }

        public String getVer() {
            return this.ver;
        }

        public String toString() {
            return this.major + "." + this.minor + "." + this.patch;
        }
    }
}

