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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class TodoClient implements ClientModInitializer, ClientStartEntrypoint {
	public static final String MOD_ID = "todo|client";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static List<Page> pages = new ArrayList<>();
	public static KeyBinding keyTodoList;
	public static String currServerName;

	@Override
	public void onInitializeClient() {
		keyTodoList = new KeyBinding("todo.key.todoList").setDefault(InputDevice.keyboard, Keyboard.KEY_M);

		LOGGER.info("Simple Todo List client initialized.");
	}

	@Override
	public void beforeClientStart() {
	}

	@Override
	public void afterClientStart() {
		GameSettings.keys.add(keyTodoList);
		OptionsPages.CONTROLS
			.withComponent(new OptionsCategory("todo.gui.options.page.controls.category.todo")
				.withComponent(new KeyBindingComponent(keyTodoList)));
	}

	public static void write() throws IOException {
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

		rootTag.put("pages", pagesList);

		Minecraft mc = Minecraft.getMinecraft();
		Path safePathDir;
		if (mc.isMultiplayerWorld()) {
			safePathDir = Paths.get("/todo/mp/" + currServerName.replaceAll("[^A-Za-z0-9]", "a")).normalize();
		} else {
			safePathDir = Paths.get("/saves/" + ((IWorldDir) mc).todo$getWorldDirName()).normalize();
		}

		File file = new File(Global.accessor.getMinecraftDir(), safePathDir + "/todo.dat");
		if (!Files.exists(file.toPath().getParent())) {
			Files.createDirectories(file.toPath().getParent());
		}

		NbtIo.writeCompressed(rootTag, Files.newOutputStream(file.toPath()));
	}

	public static void read() throws IOException {
		Minecraft mc = Minecraft.getMinecraft();
		Path safePathDir;
		if (mc.isMultiplayerWorld()) {
			safePathDir = Paths.get("/todo/mp/" + currServerName.replaceAll("[^A-Za-z0-9]", "a")).normalize();
		} else {
			safePathDir = Paths.get("/saves/" + ((IWorldDir) mc).todo$getWorldDirName()).normalize();
		}

		File file = new File(Global.accessor.getMinecraftDir(), safePathDir + "/todo.dat");
		CompoundTag rootTag = NbtIo.readCompressed(Files.newInputStream(file.toPath()));

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

	public static void addPage(Page page) {
		pages.add(page);
	}

	public static Page getPage(int i) {
		if (i >= pages.size()) {
			List<String> lines = new ArrayList<>();
			for (int strings = 0; strings < 6; strings++) {
				lines.add("");
			}

			Page newPage = new Page(i, (byte) 0, lines);
			addPage(newPage);
			return newPage;
		} else {
			return pages.get(i);
		}
	}
}
