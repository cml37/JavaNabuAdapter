package com.lenderman.nabu.adapter.extensions;

/*
 * Copyright(c) 2023 "RetroTech" Chris Lenderman
 * Copyright(c) 2022 NabuNetwork.com
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.lenderman.nabu.adapter.model.settings.Settings;
import com.lenderman.nabu.adapter.model.settings.Target;
import com.lenderman.nabu.adapter.model.settings.Target.TargetEnum;
import com.lenderman.nabu.adapter.server.NabuServer;
import com.lenderman.nabu.adapter.server.ServerInputOutputController;

public class HeadlessExtension implements ServerExtension
{
    /**
     * Class Logger
     */
    private static final Logger logger = LogManager
            .getLogger(HeadlessExtension.class);

    /**
     * Instance of the Server I/O Controller
     */
    private ServerInputOutputController sioc;

    /**
     * Instance of the NABU Server
     */
    private NabuServer server;

    /**
     * Program settings
     */
    private Settings settings;

    /**
     * Constructor
     * 
     * @param NabuServer
     */
    public HeadlessExtension(NabuServer server,
            ServerInputOutputController sioc, Settings settings)
    {
        this.server = server;
        this.sioc = sioc;
        this.settings = settings;
    }

    /**
     * Reset the extension
     */
    @Override
    public void reset()
    {
        // Do nothing
    }

    /**
     * This extension implements several new op codes - This function maps those
     * codes to the appropriate function call.
     * 
     * @param int opCode
     * @return true if we acted on this opCode, false otherwise.
     */
    @Override
    public boolean tryProcessCommand(int opCode)
    {
        try
        {
            switch (opCode)
            {
            case 0x20:
                // Send the main menu
                this.sendMenuCount();
                return true;
            case 0x21:
                // send specified menu item
                this.sendMenuItem();
                return true;
            case 0x22:
                // Select the specified cycle
                this.setCycle();
                return true;
            case 0x23:
                // set the specified local path
                this.setPath();
                return true;
            }
        }
        catch (Exception ex)
        {
            logger.error("Could not process command: ", ex);
        }

        // Op code not serviced by this extension
        return false;
    }

    /**
     * The NABU will set the cycle
     */
    private void setCycle() throws Exception
    {
        // Read the value
        int menu = this.sioc.getIs().readByte();

        // Read the menu option
        // Get the item number:
        int menuItem = this.sioc.getIs().readByte();

        List<String> names = new ArrayList<String>();

        if (settings.getSourceLocation() != Settings.SourceLocation.Headless)
        {
            // Don't set location unless in headless mode.
            return;
        }

        List<Target> cycles = settings.getCycles().getTargets();

        switch (menu)
        {
        case 1:
            names = cycles.stream()
                    .filter(t -> t.getTargetType() == TargetEnum.NabuNetwork)
                    .map(Target::getName).collect(Collectors.toList());
            break;
        case 2:
            names = cycles.stream()
                    .filter(t -> t.getTargetType() == TargetEnum.Homebrew)
                    .map(Target::getName).sorted().collect(Collectors.toList());
            break;
        case 3:
            names = cycles.stream()
                    .filter(t -> t.getTargetType() == TargetEnum.Gameroom)
                    .map(Target::getName).sorted().collect(Collectors.toList());
            break;
        }

        String name = names.get(menuItem);
        Target selected = cycles.stream().filter(t -> t.getName().equals(name))
                .findFirst().get();
        this.server.resetCycle(selected.getUrl());
    }

    /**
     * The NABU will set the path
     */
    private void setPath() throws Exception
    {
        int strlen = this.sioc.getIs().readByte();
        String path = this.sioc.getIs().readString(strlen);

        if (this.settings
                .getSourceLocation() != Settings.SourceLocation.Headless)
        {
            // Don't set location unless in headless mode.
            return;
        }

        if (path.toLowerCase().startsWith("http"))
        {
            URI url = new URI(path);

            // is this Url valid?
            if (Settings.allowedUri.contains(url.getHost().toLowerCase()))
            {
                this.server.resetCycle(path);
            }
        }
        else
        {
            // two things about headless - First, must be in the current working
            // directory and must either be a directory or .nabu
            Path fullPath = Paths.get(System.getProperty("user.dir"), path);
            if (fullPath.toFile().exists() && !fullPath.toFile().isDirectory()
                    && (path.toLowerCase().endsWith("")
                            || path.toLowerCase().endsWith(".nabu")
                            || path.toLowerCase().endsWith(".pak")))
            {
                logger.debug("Valid path: {}", fullPath.toString());
                server.resetCycle(fullPath.toString());
            }
        }
    }

    /**
     * Get the list of menu names for the NABU
     */
    private List<String> getMenuNames() throws Exception
    {
        // Get the menu number
        int menu = sioc.getIs().readByte();
        List<String> names = new ArrayList<String>();
        List<Target> cycles = settings.getCycles().getTargets();

        switch (menu)
        {
        case 0:
            names = Settings.topLevelHeadlessMenu;
            break;
        case 1:
            names = cycles.stream()
                    .filter(t -> t.getTargetType() == TargetEnum.NabuNetwork)
                    .map(Target::getName).collect(Collectors.toList());
            break;
        case 2:
            names = cycles.stream()
                    .filter(t -> t.getTargetType() == TargetEnum.Homebrew)
                    .map(Target::getName).sorted().collect(Collectors.toList());
            break;
        case 3:
            names = cycles.stream()
                    .filter(t -> t.getTargetType() == TargetEnum.Gameroom)
                    .map(Target::getName).sorted().collect(Collectors.toList());
            break;
        }
        return names;
    }

    /**
     * Send the menu count to the NABU
     */
    private void sendMenuCount() throws Exception
    {
        this.sioc.getOs().writeBytes(getMenuNames().size());
    }

    /**
     * Send the menu item to the NABU
     */
    private void sendMenuItem() throws Exception
    {
        List<String> names = getMenuNames();

        // Get the item number:
        int menuItem = this.sioc.getIs().readByte();

        // Write it out!
        this.sioc.getOs().writeString(names.get(menuItem));
    }
}
