package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.data.model.AppLanguage
import com.example.data.model.UserRole
import com.example.ui.theme.*
import com.example.ui.translation.AgroStrings
import com.example.ui.viewmodel.AgroViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    viewModel: AgroViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    val currentUser by viewModel.userState.collectAsStateWithLifecycle()
    val appLanguage by viewModel.appLanguage.collectAsStateWithLifecycle()

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var notificationsToggle by remember { mutableStateOf(currentUser?.notificationEnabled ?: true) }

    BackHandler {
        navController.popBackStack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (appLanguage == AppLanguage.TAMIL) "பயனர் சுயவிவரம்" else "User Profile & Security",
                            color = TextLight,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "RBAC Identity & Vault Credentials",
                            color = GlowingEmerald,
                            fontSize = 11.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.testTag("profile_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = GlowingEmerald
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DeepForestBg
                )
            )
        },
        containerColor = DeepForestBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val user = currentUser

            if (user == null) {
                // Session Expired / Not logged in state
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().padding(top = 40.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Filled.LockClock, contentDescription = null, tint = AlertCritical, modifier = Modifier.size(56.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Session Expired", color = TextLight, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "Your authentication session is no longer active. Please sign in again.",
                            color = TextMuted,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = {
                                navController.navigate("auth") {
                                    popUpTo(0) { inclusive = true }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GlowingEmerald)
                        ) {
                            Text("Go to Sign In", color = DeepForestBg, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                // User Avatar and Role Badge Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                    border = BorderStroke(1.dp, SoftGrass),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(88.dp)
                                .clip(CircleShape)
                                .background(GlowingEmerald.copy(alpha = 0.15f))
                        ) {
                            Icon(
                                imageVector = when (user.userRole) {
                                    UserRole.ADMIN -> Icons.Filled.AdminPanelSettings
                                    UserRole.RESEARCHER -> Icons.Filled.Biotech
                                    UserRole.FARMER -> Icons.Filled.Person
                                },
                                contentDescription = "Avatar",
                                tint = GlowingEmerald,
                                modifier = Modifier.size(48.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = user.displayName,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextLight
                        )

                        Text(
                            text = user.email,
                            fontSize = 13.sp,
                            color = TextMuted,
                            modifier = Modifier.padding(top = 2.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Role and Status Pill
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = when (user.userRole) {
                                    UserRole.ADMIN -> AlertCritical.copy(alpha = 0.2f)
                                    UserRole.RESEARCHER -> GlowingEmerald.copy(alpha = 0.2f)
                                    UserRole.FARMER -> SoftGrass.copy(alpha = 0.3f)
                                },
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, when (user.userRole) {
                                    UserRole.ADMIN -> AlertCritical
                                    UserRole.RESEARCHER -> GlowingEmerald
                                    UserRole.FARMER -> SoftGrass
                                })
                            ) {
                                Text(
                                    text = "ROLE: ${user.role}",
                                    color = when (user.userRole) {
                                        UserRole.ADMIN -> AlertCritical
                                        UserRole.RESEARCHER -> GlowingEmerald
                                        UserRole.FARMER -> TextLight
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }

                            Surface(
                                color = GlowingEmerald.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, GlowingEmerald)
                            ) {
                                Text(
                                    text = "STATUS: ${user.status}",
                                    color = GlowingEmerald,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Profile Information Block
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                    border = BorderStroke(1.dp, SoftGrass.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Account Details",
                                color = GlowingEmerald,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(
                                onClick = { showEditProfileDialog = true },
                                modifier = Modifier.size(32.dp).testTag("edit_profile_btn")
                            ) {
                                Icon(Icons.Filled.Edit, contentDescription = "Edit Profile", tint = GlowingEmerald, modifier = Modifier.size(18.dp))
                            }
                        }

                        HorizontalDivider(color = SoftGrass.copy(alpha = 0.4f), modifier = Modifier.padding(vertical = 8.dp))

                        ProfileRowItem(icon = Icons.Filled.Phone, label = "Phone Number", value = user.phoneNumber.ifBlank { "Not provided" })
                        ProfileRowItem(icon = Icons.Filled.LocationOn, label = "Farm / Facility Location", value = user.farmLocation.ifBlank { "Not configured" })
                        ProfileRowItem(
                            icon = Icons.Filled.CalendarMonth,
                            label = "Member Since",
                            value = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(user.joinedTimestamp))
                        )
                        ProfileRowItem(
                            icon = Icons.Filled.Schedule,
                            label = "Last Active Login",
                            value = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(user.lastLoginTimestamp))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Security & Vault Actions
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                    border = BorderStroke(1.dp, SoftGrass.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Security & Encryption",
                            color = GlowingEmerald,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Change Password Button
                        OutlinedButton(
                            onClick = { showChangePasswordDialog = true },
                            border = BorderStroke(1.dp, GlowingEmerald.copy(alpha = 0.8f)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().testTag("change_password_btn")
                        ) {
                            Icon(Icons.Filled.Key, contentDescription = null, tint = GlowingEmerald, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Change Vault Password", color = GlowingEmerald, fontWeight = FontWeight.SemiBold)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Language Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Translate, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Language (English / தமிழ்)", color = TextLight, fontSize = 13.sp)
                            }
                            Switch(
                                checked = appLanguage == AppLanguage.TAMIL,
                                onCheckedChange = { _ ->
                                    val newLang = if (appLanguage == AppLanguage.TAMIL) AppLanguage.ENGLISH else AppLanguage.TAMIL
                                    viewModel.toggleAppLanguage()
                                    viewModel.updateSettings(notificationsToggle, newLang.code)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = DeepForestBg,
                                    checkedTrackColor = GlowingEmerald
                                )
                            )
                        }

                        // Notification Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Notifications, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Push Stress Alerts", color = TextLight, fontSize = 13.sp)
                            }
                            Switch(
                                checked = notificationsToggle,
                                onCheckedChange = { checked ->
                                    notificationsToggle = checked
                                    viewModel.updateSettings(checked, appLanguage.code)
                                    Toast.makeText(context, if (checked) "Alert notifications enabled" else "Alert notifications muted", Toast.LENGTH_SHORT).show()
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = DeepForestBg,
                                    checkedTrackColor = GlowingEmerald
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Account Danger Zone
                Card(
                    colors = CardDefaults.cardColors(containerColor = AlertCritical.copy(alpha = 0.08f)),
                    border = BorderStroke(1.dp, AlertCritical.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Account Actions",
                            color = AlertCritical,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Log out from this terminal or permanently purge personal biometric telemetry data.",
                            color = TextMuted,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    viewModel.logout()
                                    navController.navigate("auth") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                },
                                border = BorderStroke(1.dp, TextMuted),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).testTag("profile_logout_btn")
                            ) {
                                Icon(Icons.Filled.ExitToApp, contentDescription = null, tint = TextLight, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Sign Out", color = TextLight, fontSize = 13.sp)
                            }

                            Button(
                                onClick = { showDeleteAccountDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = AlertCritical),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).testTag("profile_delete_account_btn")
                            ) {
                                Icon(Icons.Filled.DeleteForever, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Delete Account", color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    // --- DIALOG 1: Edit Profile Dialog ---
    if (showEditProfileDialog && currentUser != null) {
        val user = currentUser!!
        var editName by remember { mutableStateOf(user.displayName) }
        var editPhone by remember { mutableStateOf(user.phoneNumber) }
        var editLocation by remember { mutableStateOf(user.farmLocation) }
        var errorMsg by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            containerColor = CardSurfaceDark,
            title = {
                Text("Edit Profile Details", color = GlowingEmerald, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Display Name") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GlowingEmerald,
                            focusedLabelColor = GlowingEmerald,
                            unfocusedBorderColor = SoftGrass,
                            focusedTextColor = TextLight,
                            unfocusedTextColor = TextLight
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("edit_name_input"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = editPhone,
                        onValueChange = { editPhone = it },
                        label = { Text("Phone Number") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GlowingEmerald,
                            focusedLabelColor = GlowingEmerald,
                            unfocusedBorderColor = SoftGrass,
                            focusedTextColor = TextLight,
                            unfocusedTextColor = TextLight
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("edit_phone_input"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = editLocation,
                        onValueChange = { editLocation = it },
                        label = { Text("Farm / Facility Location") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GlowingEmerald,
                            focusedLabelColor = GlowingEmerald,
                            unfocusedBorderColor = SoftGrass,
                            focusedTextColor = TextLight,
                            unfocusedTextColor = TextLight
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("edit_location_input"),
                        singleLine = true
                    )

                    errorMsg?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(it, color = AlertCritical, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editName.isBlank()) {
                            errorMsg = "Name cannot be empty."
                            return@Button
                        }
                        viewModel.updateProfile(editName, editPhone, editLocation) { success, err ->
                            if (success) {
                                Toast.makeText(context, "Profile updated successfully.", Toast.LENGTH_SHORT).show()
                                showEditProfileDialog = false
                            } else {
                                errorMsg = err ?: "Failed to update profile."
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GlowingEmerald),
                    modifier = Modifier.testTag("save_profile_btn")
                ) {
                    Text("Save Changes", color = DeepForestBg, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }

    // --- DIALOG 2: Change Password Dialog ---
    if (showChangePasswordDialog) {
        var oldPassword by remember { mutableStateOf("") }
        var newPassword by remember { mutableStateOf("") }
        var confirmPassword by remember { mutableStateOf("") }
        var errorMsg by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showChangePasswordDialog = false },
            containerColor = CardSurfaceDark,
            title = {
                Text("Change Vault Password", color = GlowingEmerald, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = oldPassword,
                        onValueChange = { oldPassword = it },
                        label = { Text("Current Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GlowingEmerald,
                            focusedLabelColor = GlowingEmerald,
                            unfocusedBorderColor = SoftGrass,
                            focusedTextColor = TextLight,
                            unfocusedTextColor = TextLight
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("current_password_input"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("New Password (min 6 chars)") },
                        visualTransformation = PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GlowingEmerald,
                            focusedLabelColor = GlowingEmerald,
                            unfocusedBorderColor = SoftGrass,
                            focusedTextColor = TextLight,
                            unfocusedTextColor = TextLight
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("new_password_input"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirm New Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GlowingEmerald,
                            focusedLabelColor = GlowingEmerald,
                            unfocusedBorderColor = SoftGrass,
                            focusedTextColor = TextLight,
                            unfocusedTextColor = TextLight
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("confirm_password_input"),
                        singleLine = true
                    )

                    errorMsg?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(it, color = AlertCritical, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPassword != confirmPassword) {
                            errorMsg = "New passwords do not match."
                            return@Button
                        }
                        if (newPassword.length < 6) {
                            errorMsg = "Password must be at least 6 characters."
                            return@Button
                        }
                        viewModel.changePassword(oldPassword, newPassword) { success, err ->
                            if (success) {
                                Toast.makeText(context, "Password updated successfully.", Toast.LENGTH_SHORT).show()
                                showChangePasswordDialog = false
                            } else {
                                errorMsg = err ?: "Failed to update password."
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GlowingEmerald),
                    modifier = Modifier.testTag("submit_change_password_btn")
                ) {
                    Text("Update Password", color = DeepForestBg, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangePasswordDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }

    // --- DIALOG 3: Delete Account Confirmation Dialog ---
    if (showDeleteAccountDialog) {
        var confirmationText by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            containerColor = CardSurfaceDark,
            title = {
                Text("Delete Account & Data?", color = AlertCritical, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column {
                    Text(
                        "This will permanently delete your account profile, linked sensor records, and AI scan telemetry. This action cannot be reversed.",
                        color = TextLight,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Type 'DELETE' to confirm:",
                        color = TextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = confirmationText,
                        onValueChange = { confirmationText = it },
                        placeholder = { Text("DELETE") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AlertCritical,
                            unfocusedBorderColor = SoftGrass,
                            focusedTextColor = TextLight,
                            unfocusedTextColor = TextLight
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("delete_confirm_input"),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (confirmationText.trim() == "DELETE") {
                            viewModel.deleteAccount {
                                Toast.makeText(context, "Account permanently removed.", Toast.LENGTH_SHORT).show()
                                showDeleteAccountDialog = false
                                navController.navigate("auth") {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        } else {
                            Toast.makeText(context, "Please type DELETE to confirm.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = confirmationText.trim() == "DELETE",
                    colors = ButtonDefaults.buttonColors(containerColor = AlertCritical),
                    modifier = Modifier.testTag("finalize_delete_account_btn")
                ) {
                    Text("Purge Account", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }
}

@Composable
private fun ProfileRowItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = GlowingEmerald, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(label, color = TextMuted, fontSize = 11.sp)
            Text(value, color = TextLight, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
    }
}
