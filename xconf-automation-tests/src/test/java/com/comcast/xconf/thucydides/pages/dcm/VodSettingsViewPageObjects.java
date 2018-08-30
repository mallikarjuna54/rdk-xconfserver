/* 
 * If not stated otherwise in this file or this component's Licenses.txt file the 
 * following copyright and licenses apply:
 *
 * Copyright 2018 RDK Management
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p>
 * Author: Stanislav Menshykov
 * Created: 3/29/16  3:34 PM
 */
package com.comcast.xconf.thucydides.pages.dcm;

import net.thucydides.core.annotations.findby.FindBy;
import net.thucydides.core.pages.PageObject;
import net.thucydides.core.pages.WebElementFacade;
import org.openqa.selenium.WebDriver;

public class VodSettingsViewPageObjects extends PageObject {

    public VodSettingsViewPageObjects(WebDriver webDriver) {
        super(webDriver);
    }

    @FindBy(css = "input#vodSettingsName")
    private WebElementFacade name;

    @FindBy(css = "input#locationsUrl")
    private WebElementFacade locationsUrl;

    @FindBy(css = "span.srm-name")
    private WebElementFacade srmName;

    @FindBy(css = "span.srm-ip")
    private WebElementFacade srmIp;

    public String getName() {
        return name.getValue();
    }

    public String getLocationsUrl() {
        return locationsUrl.getValue();
    }

    public String getSrmName() {
        return srmName.getText();
    }

    public String getSrmIp() {
        return srmIp.getText();
    }
}
