package cookie.todo.extra.mixin;

import cookie.todo.TodoClient;
import cookie.todo.client.gui.ScreenTodo;
import cookie.todo.extra.interfaces.IWorldDir;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.input.InputDevice;
import net.minecraft.core.MinecraftAccessor;
import net.minecraft.core.world.type.WorldTypeGroups;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(value = Minecraft.class, remap = false)
public abstract class MinecraftMixin implements MinecraftAccessor, IWorldDir {
	@Unique
	public String worldDirName;

	@Shadow
	public abstract void displayScreen(Screen screen);

	@Shadow
	@Nullable
	public Screen currentScreen;

	@Inject(method = "checkBoundInputs", at = @At("TAIL"))
	private void todo_tickButtonPress(InputDevice currentInputDevice, CallbackInfoReturnable<Boolean> cir) {
		if (TodoClient.keyTodoList.isPressed() && currentScreen == null) {
			displayScreen(new ScreenTodo());
		}
	}

	@Inject(method = "startWorld(Ljava/lang/String;Ljava/lang/String;JLnet/minecraft/core/world/type/WorldTypeGroups$Group;)V", at = @At("HEAD"))
	private void todo_setWorldDirName(String worldDirName, String worldName, long seed, WorldTypeGroups.Group worldTypeGroup, CallbackInfo ci) {
		this.worldDirName = worldDirName;
	}

	@Override
	public String todo$getWorldDirName() {
		return worldDirName;
	}
}
