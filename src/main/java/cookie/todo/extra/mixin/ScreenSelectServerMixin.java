package cookie.todo.extra.mixin;

import cookie.todo.TodoClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.gui.ScreenSelectServer;
import net.minecraft.core.net.SavedServerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(value = ScreenSelectServer.class, remap = false)
public abstract class ScreenSelectServerMixin extends Screen {
	@Shadow
	private SavedServerList savedServerList;
	@Shadow
	private int selectedServer;

	@Inject(method = "selectServer", at = @At("TAIL"))
	private void todo_setCurrServerIP(int i, CallbackInfo ci) {
		TodoClient.currServerName = savedServerList.servers.get(selectedServer).nickname;
	}
}
