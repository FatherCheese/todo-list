package cookie.todo;

import com.mojang.nbt.NbtIo;
import com.mojang.nbt.tags.ByteTag;
import com.mojang.nbt.tags.CompoundTag;
import com.mojang.nbt.tags.ListTag;
import com.mojang.nbt.tags.StringTag;
import cookie.todo.client.Page;
import cookie.todo.extra.interfaces.IWorldDir;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.options.components.KeyBindingComponent;
import net.minecraft.client.gui.options.components.OptionsCategory;
import net.minecraft.client.gui.options.data.OptionsPages;
import net.minecraft.client.input.InputDevice;
import net.minecraft.client.option.GameSettings;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.core.Global;
import org.lwjgl.input.Keyboard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import turniplabs.halplibe.util.ClientStartEntrypoint;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * The client mod initializer class.
 */
@Environment(EnvType.CLIENT)
public class TodoClient implements ClientModInitializer, ClientStartEntrypoint {
	public static final String MOD_ID = "todo|client";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static List<Page> pages = new ArrayList<>();
	public static KeyBinding keyTodoList;
	public static String currServerName;

	/**
	 * A method handling initializing the client.
	 */
	@Override
	public void onInitializeClient() {
		keyTodoList = new KeyBinding("todo.key.todoList").setDefault(InputDevice.keyboard, Keyboard.KEY_M);

		LOGGER.info("Simple Todo List client initialized.");
	}

	@Override
	public void beforeClientStart() {
	}

	/**
	 * A method to handle functions after the client has started.
	 */
	@Override
	public void afterClientStart() {
		GameSettings.keys.add(keyTodoList);
		OptionsPages.CONTROLS
			.withComponent(new OptionsCategory("todo.options.category.todo")
				.withComponent(new KeyBindingComponent(keyTodoList)));
	}

	/**
	 * A method to write the NBT data for the world list.
	 */
	public static void write() {
		// First, we try to clean up any extra pages and nudge
		//  the ones above that down.
		cleanupEmptyPages();

		// Then we check through the static pages list. For every line
		// we add it to our pagesList as a new string tag.
		// Checkboxes are also stored as a byte tag. (Cheaper than a boolean!)
		CompoundTag rootTag = new CompoundTag();
		ListTag pagesList = new ListTag();

		for (Page page : pages) {
			CompoundTag pageTag = new CompoundTag();
			ListTag lines = new ListTag();

			for (String line : page.lines) {
				lines.addTag(new StringTag(line));
			}

			pageTag.put("lines", lines);
			ByteTag checkboxes = new ByteTag(page.checkboxes);
			pageTag.put("checkboxes", checkboxes);
			pagesList.addTag(pageTag);
		}

		// Now we store the pages into the root tag.
		rootTag.put("pages", pagesList);

		// Afterward, we get the Minecraft client and create a
		// new Path variable. Then we check if the world is MP or
		// SP. If MP, create/get the server directory. Otherwise,
		// we store it inside the world file itself.
		Minecraft mc = Minecraft.getMinecraft();
		Path safePathDir;
		if (mc.isMultiplayerWorld()) {
			if (currServerName == null || currServerName.isEmpty()) currServerName = "server";

			safePathDir = Paths.get("/todo/mp/" + currServerName
				.replaceAll("[^A-Za-z0-9]", "a"))
				.normalize();
		} else safePathDir = Paths.get("/saves/" + ((IWorldDir) mc).todo$getWorldDirName()).normalize();

		// Now we check if we can resolve the path to the .dat file.
		// If not, we try to make a new one and log an error if unsuccessful.
		File file = new File(Global.accessor.getMinecraftDir(), safePathDir.resolve("todo.dat").toString());
		if (!Files.exists(file.toPath().getParent())) {
			try {
				Files.createDirectories(file.toPath().getParent());
			} catch (IOException e) {
				LOGGER.error("Failed to create the data directory!");
				LOGGER.error(e.getLocalizedMessage(), e);
			}
		}

		// Now we try to actually write the root tag to the .dat file.
		// We log an error if it's unsuccessful.
		try (OutputStream out = Files.newOutputStream(file.toPath())) {
			NbtIo.writeCompressed(rootTag, out);
		} catch (IOException e) {
			LOGGER.error("Couldn't write {} to {} due to an IOException!",
				file.getName(),
				file.getAbsolutePath());
			LOGGER.error(e.getLocalizedMessage(), e);
		}
	}

	/**
	 * A method to clean up empty pages. Aka pages with
	 * no checkboxes or text.
	 */
	private static void cleanupEmptyPages() {
		for (int i = pages.size() - 1; i >= 0; i--) {
			Page page = pages.get(i);
			boolean isEmpty = true;

			for (String line : page.lines) {
				if (!line.trim().isEmpty()) {
					isEmpty = false;
					break;
				}
			}

			if (page.checkboxes != 0) isEmpty = false;

			if (isEmpty) {
				pages.remove(i);

				for (int j = i; j < pages.size(); j++) {
					pages.get(j).id = j;
				}
			}
		}
	}

	/**
	 * A method to read the NBT data for the world list.
	 */
	public static void read() {
		// First, we create some variables, such as getting
		// the Minecraft client, and a new path. Then we check
		// if the world is MP or SP. If it's MP, we check the
		// page folder. Otherwise, we check the world file.
		Minecraft mc = Minecraft.getMinecraft();
		Path safePathDir;
		if (mc.isMultiplayerWorld()) {
			safePathDir = Paths.get("/todo/mp/" + currServerName
				.replaceAll("[^A-Za-z0-9]", "a"))
				.normalize();
		} else {
			safePathDir = Paths.get("/saves/" + ((IWorldDir) mc).todo$getWorldDirName())
				.normalize();
		}

		// Now we resolve and get the .dat file from the path.
		File file = new File(Global.accessor.getMinecraftDir(), safePathDir.resolve("todo.dat").toString());

		// If it DOES NOT exist, we set up an empty placeholder page.
		if (!file.exists()) {
			List<String> defaultLines = new ArrayList<>();
			for (int i = 0; i < 6; i++) {
				defaultLines.add("");
			}

			pages.clear(); // Clear any existing pages
			pages.add(new Page(0, (byte) 0, new ArrayList<>(defaultLines)));
			return;
		}

		// Now we set up a root tag and try to read the file.
		// If successful, we set it to the root tag. Otherwise,
		// we log an error and return early.
		CompoundTag rootTag;
		try (InputStream in = Files.newInputStream(file.toPath())) {
			rootTag = NbtIo.readCompressed(in);
		} catch (IOException e) {
			LOGGER.error("Couldn't read {} from {} due to an IOException!\n",
				file.getName(),
				file.getAbsolutePath());

			LOGGER.error(e.getLocalizedMessage(), e);
			return;
		}

		// Now we set up a page list from the root tag's pages.
		// For each counted page, we get the checkboxes and lines.
		// We scan for 6 lines per page and add them to a list.
		// Finally, we add it to the static "pages" list.
		ListTag pagesList = rootTag.getList("pages");
		for (int page = 0; page < pagesList.tagCount(); page++) {
			CompoundTag pageCompound = (CompoundTag) pagesList.tagAt(page);
			byte checkboxes = pageCompound.getByte("checkboxes");

			ListTag linesList = pageCompound.getList("lines");
			List<String> lines = new ArrayList<>();
			for (int line = 0; line < 6; line++) {
				StringTag lineString = (StringTag) linesList.tagAt(line);
				lines.add(lineString.getValue());
			}

			pages.add(page, new Page(page, checkboxes, lines));
		}
	}

	/**
	 * A method to handle adding new pages.
	 * @param page A page to add
	 */
	public static void addPage(Page page) {
		pages.add(page);
	}

	/**
	 * A method to handle getting a specific page.
	 * @param index The page to try and get
	 * @return Returns a new page or the requested page
	 */
	public static Page getPage(int index) {
		if (index >= pages.size()) {
			List<String> lines = new ArrayList<>();
			for (int strings = 0; strings < 6; strings++) {
				lines.add("");
			}

			Page newPage = new Page(index, (byte) 0, new ArrayList<>(lines));
			addPage(newPage);
			return newPage;
		} else {
			return pages.get(index);
		}
	}

}
