package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.AgroViewModel
import java.text.SimpleDateFormat
import java.util.*

enum class AdminTab(val title: String, val icon: ImageVector) {
    USERS("Users & RBAC", Icons.Filled.People),
    CROPS("Crops", Icons.Filled.Spa),
    SENSORS("Sensors", Icons.Filled.Sensors),
    SCANS("AI Scans", Icons.Filled.PhotoCamera),
    HISTORY("History", Icons.Filled.ShowChart),
    ALERTS("Alerts", Icons.Filled.Notifications),
    FEEDBACK("Feedback", Icons.Filled.RateReview),
    MAINTENANCE("DB Maintenance", Icons.Filled.Storage)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    viewModel: AgroViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(AdminTab.CROPS) }

    val allUsers by viewModel.allUsers.collectAsStateWithLifecycle()
    val plants by viewModel.plants.collectAsStateWithLifecycle()
    val sensorHistory by viewModel.sensorHistory.collectAsStateWithLifecycle()
    val allPredictions by viewModel.allPredictions.collectAsStateWithLifecycle()
    val allHistoryData by viewModel.allHistoryData.collectAsStateWithLifecycle()
    val activeAlerts by viewModel.activeAlerts.collectAsStateWithLifecycle()
    val feedbackList by viewModel.feedbackList.collectAsStateWithLifecycle()
    val adminMessage by viewModel.adminActionMessage.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()

    LaunchedEffect(adminMessage) {
        adminMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearAdminActionMessage()
        }
    }

    // Role Guard: Check if currentUser is Admin
    val currentUser by viewModel.userState.collectAsStateWithLifecycle()
    if (currentUser?.isAdmin != true) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Admin Console Restricted", color = AlertCritical, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = GlowingEmerald)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = DeepForestBg)
                )
            },
            containerColor = DeepForestBg
        ) { padding ->
            Box(
                modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                    border = BorderStroke(1.dp, AlertCritical.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth().testTag("unauthorized_admin_card")
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Filled.Lock, contentDescription = null, tint = AlertCritical, modifier = Modifier.size(56.dp))
                        Spacer(modifier = Modifier.height(14.dp))
                        Text("Access Denied: Admin Privileges Required", color = AlertCritical, fontSize = 16.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "You are currently signed in as '${currentUser?.role ?: "GUEST"}'. Root configuration and user management requires the 'ADMIN' role.",
                            color = TextLight,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { navController.popBackStack() },
                            colors = ButtonDefaults.buttonColors(containerColor = GlowingEmerald)
                        ) {
                            Text("Back to Application", color = DeepForestBg, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.AdminPanelSettings,
                                contentDescription = "Admin",
                                tint = GlowingEmerald,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Admin Control Center",
                                color = TextLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                        Text(
                            text = "Centralized Data & Configuration Hub",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.testTag("admin_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextLight
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.triggerManualSync() },
                        enabled = !isSyncing,
                        modifier = Modifier.testTag("admin_sync_btn")
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = GlowingEmerald,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.Sync,
                                contentDescription = "Sync",
                                tint = GlowingEmerald
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DeepForestBg,
                    titleContentColor = TextLight
                )
            )
        },
        containerColor = DeepForestBg
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Admin stats overview chip bar
            Card(
                colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                border = BorderStroke(1.dp, SoftGrass),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(if (isOnline) GlowingEmerald else AlertWarning, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isOnline) "Cloud & Room Synced" else "Offline Room DB",
                            color = if (isOnline) GlowingEmerald else AlertWarning,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Total Records: ${plants.size + sensorHistory.size + allPredictions.size + allHistoryData.size + activeAlerts.size + feedbackList.size}",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            // Scrollable Tab Category Navigation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AdminTab.values().forEach { tab ->
                    val isSelected = selectedTab == tab
                    val badgeCount = when (tab) {
                        AdminTab.USERS -> allUsers.size
                        AdminTab.CROPS -> plants.size
                        AdminTab.SENSORS -> sensorHistory.size
                        AdminTab.SCANS -> allPredictions.size
                        AdminTab.HISTORY -> allHistoryData.size
                        AdminTab.ALERTS -> activeAlerts.size
                        AdminTab.FEEDBACK -> feedbackList.size
                        AdminTab.MAINTENANCE -> null
                    }

                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedTab = tab },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(tab.title)
                                if (badgeCount != null) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "($badgeCount)",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) DeepForestBg else GlowingEmerald
                                    )
                                }
                            }
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GlowingEmerald,
                            selectedLabelColor = DeepForestBg,
                            selectedLeadingIconColor = DeepForestBg,
                            containerColor = CardSurfaceDark,
                            labelColor = TextLight,
                            iconColor = TextMuted
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = SoftGrass,
                            selectedBorderColor = GlowingEmerald
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Main Tab Content Panel
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                when (selectedTab) {
                    AdminTab.USERS -> AdminUsersTab(users = allUsers, viewModel = viewModel)
                    AdminTab.CROPS -> AdminCropsTab(plants = plants, viewModel = viewModel)
                    AdminTab.SENSORS -> AdminSensorsTab(sensors = sensorHistory, viewModel = viewModel)
                    AdminTab.SCANS -> AdminScansTab(predictions = allPredictions, plants = plants, viewModel = viewModel)
                    AdminTab.HISTORY -> AdminHistoryTab(histories = allHistoryData, plants = plants, viewModel = viewModel)
                    AdminTab.ALERTS -> AdminAlertsTab(alerts = activeAlerts, viewModel = viewModel)
                    AdminTab.FEEDBACK -> AdminFeedbackTab(feedbackList = feedbackList, viewModel = viewModel)
                    AdminTab.MAINTENANCE -> AdminMaintenanceTab(
                        plants = plants,
                        sensors = sensorHistory,
                        predictions = allPredictions,
                        histories = allHistoryData,
                        alerts = activeAlerts,
                        feedbackList = feedbackList,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

// ==========================================
// 0. USERS & RBAC MANAGEMENT TAB
// ==========================================
@Composable
fun AdminUsersTab(
    users: List<User>,
    viewModel: AgroViewModel
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedRoleFilter by remember { mutableStateOf("ALL") }
    var userToEdit by remember { mutableStateOf<User?>(null) }
    var userToDelete by remember { mutableStateOf<User?>(null) }

    val filteredUsers = remember(users, searchQuery, selectedRoleFilter) {
        users.filter { u ->
            val matchesQuery = searchQuery.isBlank() ||
                u.displayName.contains(searchQuery, ignoreCase = true) ||
                u.email.contains(searchQuery, ignoreCase = true) ||
                u.farmLocation.contains(searchQuery, ignoreCase = true)
            val matchesRole = selectedRoleFilter == "ALL" || u.role.equals(selectedRoleFilter, ignoreCase = true)
            matchesQuery && matchesRole
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Search and Filter Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by name, email, or farm...") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = GlowingEmerald) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GlowingEmerald,
                focusedLabelColor = GlowingEmerald,
                unfocusedBorderColor = SoftGrass,
                focusedTextColor = TextLight,
                unfocusedTextColor = TextLight
            ),
            modifier = Modifier.fillMaxWidth().testTag("admin_user_search"),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Role filter pills
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("ALL", "FARMER", "RESEARCHER", "ADMIN").forEach { roleKey ->
                val isSelected = selectedRoleFilter == roleKey
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedRoleFilter = roleKey },
                    label = { Text(roleKey, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GlowingEmerald,
                        selectedLabelColor = DeepForestBg,
                        containerColor = CardSurfaceDark,
                        labelColor = TextLight
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = SoftGrass,
                        selectedBorderColor = GlowingEmerald
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "REGISTERED ACCOUNTS (${filteredUsers.size})",
            color = GlowingEmerald,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (filteredUsers.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No user accounts match current criteria.", color = TextMuted, fontSize = 13.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredUsers, key = { it.id }) { user ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                        border = BorderStroke(1.dp, if (user.status == "SUSPENDED") AlertCritical.copy(alpha = 0.6f) else SoftGrass),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().testTag("user_card_${user.id}")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when (user.userRole) {
                                                    UserRole.ADMIN -> AlertCritical.copy(alpha = 0.2f)
                                                    UserRole.RESEARCHER -> GlowingEmerald.copy(alpha = 0.2f)
                                                    UserRole.FARMER -> SoftGrass.copy(alpha = 0.3f)
                                                }
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = when (user.userRole) {
                                                UserRole.ADMIN -> Icons.Filled.AdminPanelSettings
                                                UserRole.RESEARCHER -> Icons.Filled.Biotech
                                                UserRole.FARMER -> Icons.Filled.Person
                                            },
                                            contentDescription = null,
                                            tint = when (user.userRole) {
                                                UserRole.ADMIN -> AlertCritical
                                                UserRole.RESEARCHER -> GlowingEmerald
                                                UserRole.FARMER -> TextLight
                                            },
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(user.displayName, color = TextLight, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                        Text(user.email, color = TextMuted, fontSize = 11.sp)
                                    }
                                }

                                // Status badge
                                Surface(
                                    color = if (user.status == "ACTIVE") GlowingEmerald.copy(alpha = 0.15f) else AlertCritical.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, if (user.status == "ACTIVE") GlowingEmerald else AlertCritical)
                                ) {
                                    Text(
                                        text = user.status,
                                        color = if (user.status == "ACTIVE") GlowingEmerald else AlertCritical,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text("Role: ${user.role} • Farm: ${user.farmLocation.ifBlank { "Not set" }}", color = TextLight, fontSize = 12.sp)
                            if (user.phoneNumber.isNotBlank()) {
                                Text("Phone: ${user.phoneNumber}", color = TextMuted, fontSize = 11.sp)
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = SoftGrass.copy(alpha = 0.4f))
                            Spacer(modifier = Modifier.height(8.dp))

                            // Action buttons: Toggle Status, Change Role, Delete
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Status toggle (Suspend / Activate)
                                OutlinedButton(
                                    onClick = {
                                        val newStatus = if (user.status == "ACTIVE") "SUSPENDED" else "ACTIVE"
                                        viewModel.adminSetUserStatus(user.id, newStatus) { success ->
                                            if (success) {
                                                Toast.makeText(context, "User status updated to $newStatus", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    },
                                    border = BorderStroke(1.dp, if (user.status == "ACTIVE") AlertWarning else GlowingEmerald),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = if (user.status == "ACTIVE") "Suspend" else "Activate",
                                        color = if (user.status == "ACTIVE") AlertWarning else GlowingEmerald,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                // Role selector trigger
                                OutlinedButton(
                                    onClick = { userToEdit = user },
                                    border = BorderStroke(1.dp, GlowingEmerald.copy(alpha = 0.7f)),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Change Role", color = GlowingEmerald, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }

                                // Delete user button
                                IconButton(
                                    onClick = { userToDelete = user },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Delete User", tint = AlertCritical, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Role assignment dialog
    userToEdit?.let { targetUser ->
        var selectedRole by remember { mutableStateOf(targetUser.role) }
        AlertDialog(
            onDismissRequest = { userToEdit = null },
            containerColor = CardSurfaceDark,
            title = { Text("Assign User Role", color = GlowingEmerald, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Select RBAC privilege tier for ${targetUser.displayName}:", color = TextLight, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    listOf("FARMER", "RESEARCHER", "ADMIN").forEach { roleOption ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedRole = roleOption }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedRole == roleOption,
                                onClick = { selectedRole = roleOption },
                                colors = RadioButtonDefaults.colors(selectedColor = GlowingEmerald)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(roleOption, color = TextLight, fontSize = 13.sp, fontWeight = if (selectedRole == roleOption) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.adminSetUserRole(targetUser.id, selectedRole) { success ->
                            if (success) {
                                Toast.makeText(context, "Role updated to $selectedRole", Toast.LENGTH_SHORT).show()
                                userToEdit = null
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GlowingEmerald)
                ) {
                    Text("Apply Role", color = DeepForestBg, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { userToEdit = null }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }

    // Delete user confirmation dialog
    userToDelete?.let { targetUser ->
        AlertDialog(
            onDismissRequest = { userToDelete = null },
            containerColor = CardSurfaceDark,
            title = { Text("Delete User Account?", color = AlertCritical, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Are you sure you want to permanently delete account '${targetUser.email}' (${targetUser.displayName})? This cannot be undone.",
                    color = TextLight,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.adminDeleteUser(targetUser.id) { success ->
                            if (success) {
                                Toast.makeText(context, "User account removed.", Toast.LENGTH_SHORT).show()
                                userToDelete = null
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AlertCritical)
                ) {
                    Text("Delete Account", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { userToDelete = null }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }
}

// ==========================================
// 1. CROPS MANAGEMENT TAB
// ==========================================
@Composable
fun AdminCropsTab(
    plants: List<Plant>,
    viewModel: AgroViewModel
) {
    var showAddCropDialog by remember { mutableStateOf(false) }
    var editingPlant by remember { mutableStateOf<Plant?>(null) }
    var plantToDelete by remember { mutableStateOf<Plant?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "REGISTERED CROPS (${plants.size})",
                color = GlowingEmerald,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )

            Button(
                onClick = { showAddCropDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = GlowingEmerald),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("admin_add_crop_btn")
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add", tint = DeepForestBg, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("New Crop", color = DeepForestBg, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        if (plants.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No crops registered in Room database.", color = TextMuted)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(plants, key = { it.id }) { plant ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                        border = BorderStroke(1.dp, SoftGrass),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = plant.name,
                                        color = TextLight,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = "Variety: ${plant.variety} • Location: ${plant.location}",
                                        color = GlowingEmerald,
                                        fontSize = 12.sp
                                    )
                                }

                                Row {
                                    IconButton(
                                        onClick = { editingPlant = plant },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = TextLight, modifier = Modifier.size(18.dp))
                                    }
                                    IconButton(
                                        onClick = { plantToDelete = plant },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = AlertCritical, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Planted: ${plant.datePlanted} • ID: #${plant.id}",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                            if (plant.notes.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Notes: ${plant.notes}",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Crop Dialog
    if (showAddCropDialog) {
        var name by remember { mutableStateOf("") }
        var variety by remember { mutableStateOf("") }
        var location by remember { mutableStateOf("Polyhouse Zone A") }
        var datePlanted by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) }
        var notes by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddCropDialog = false },
            title = { Text("Register New Crop (Room DB)", color = TextLight, fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Crop Name (e.g. Bell Pepper)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = variety,
                        onValueChange = { variety = it },
                        label = { Text("Variety (e.g. California Wonder)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Location (e.g. Bed 4)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = datePlanted,
                        onValueChange = { datePlanted = it },
                        label = { Text("Date Planted (YYYY-MM-DD)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Agronomic Notes") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank() && variety.isNotBlank()) {
                            viewModel.adminAddPlant(name, variety, datePlanted, location, notes)
                            showAddCropDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GlowingEmerald)
                ) {
                    Text("SAVE TO DATABASE", color = DeepForestBg, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCropDialog = false }) {
                    Text("CANCEL", color = TextMuted)
                }
            },
            containerColor = CardSurfaceDark
        )
    }

    // Edit Crop Dialog
    editingPlant?.let { plant ->
        var name by remember { mutableStateOf(plant.name) }
        var variety by remember { mutableStateOf(plant.variety) }
        var location by remember { mutableStateOf(plant.location) }
        var datePlanted by remember { mutableStateOf(plant.datePlanted) }
        var notes by remember { mutableStateOf(plant.notes) }

        AlertDialog(
            onDismissRequest = { editingPlant = null },
            title = { Text("Edit Crop #${plant.id}", color = TextLight, fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Crop Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = variety,
                        onValueChange = { variety = it },
                        label = { Text("Variety") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Location") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = datePlanted,
                        onValueChange = { datePlanted = it },
                        label = { Text("Date Planted") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.adminUpdatePlant(
                            plant.copy(
                                name = name,
                                variety = variety,
                                location = location,
                                datePlanted = datePlanted,
                                notes = notes
                            )
                        )
                        editingPlant = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GlowingEmerald)
                ) {
                    Text("UPDATE", color = DeepForestBg, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingPlant = null }) {
                    Text("CANCEL", color = TextMuted)
                }
            },
            containerColor = CardSurfaceDark
        )
    }

    // Delete Confirmation Dialog
    plantToDelete?.let { plant ->
        AlertDialog(
            onDismissRequest = { plantToDelete = null },
            title = { Text("Delete Crop", color = AlertCritical, fontWeight = FontWeight.Bold) },
            text = {
                Text("Are you sure you want to permanently delete '${plant.name}' and remove it from Room DB?", color = TextLight)
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.adminDeletePlant(plant.id)
                        plantToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AlertCritical)
                ) {
                    Text("CONFIRM DELETE", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { plantToDelete = null }) {
                    Text("CANCEL", color = TextMuted)
                }
            },
            containerColor = CardSurfaceDark
        )
    }
}

// ==========================================
// 2. SENSOR TELEMETRY & IOT TAB
// ==========================================
@Composable
fun AdminSensorsTab(
    sensors: List<SensorData>,
    viewModel: AgroViewModel
) {
    var showInjectDialog by remember { mutableStateOf(false) }
    var showClearConfirm by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SENSOR LOGS (${sensors.size})",
                color = GlowingEmerald,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedButton(
                    onClick = { showClearConfirm = true },
                    border = BorderStroke(1.dp, AlertCritical),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Clear All", color = AlertCritical, fontSize = 11.sp)
                }

                Button(
                    onClick = { showInjectDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = GlowingEmerald),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Inject", tint = DeepForestBg, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Inject Reading", color = DeepForestBg, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        }

        if (sensors.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No sensor records in database.", color = TextMuted)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(sensors, key = { it.id }) { sensor ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                        border = BorderStroke(1.dp, SoftGrass),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${sensor.temperature}°C",
                                        color = TextLight,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(text = " • ", color = TextMuted)
                                    Text(
                                        text = "${sensor.humidity}% RH",
                                        color = GlowingEmerald,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(text = " • ", color = TextMuted)
                                    Text(
                                        text = "Soil: ${sensor.soilMoisture}%",
                                        color = SoilMoistureBlue,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault()).format(Date(sensor.timestamp)) + " (ID: #${sensor.id})",
                                    color = TextMuted,
                                    fontSize = 10.sp
                                )
                            }

                            IconButton(
                                onClick = { viewModel.adminDeleteSensorData(sensor.id) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = AlertCritical, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // Inject Sensor Dialog
    if (showInjectDialog) {
        var tempStr by remember { mutableStateOf("24.5") }
        var humStr by remember { mutableStateOf("65.0") }
        var moistureStr by remember { mutableStateOf("45.0") }

        AlertDialog(
            onDismissRequest = { showInjectDialog = false },
            title = { Text("Inject Sensor Reading", color = TextLight, fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Quick Environmental Presets:", color = TextMuted, fontSize = 11.sp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SuggestionChip(
                            onClick = { tempStr = "24.0"; humStr = "65.0"; moistureStr = "45.0" },
                            label = { Text("Optimal (24°C/45%)", fontSize = 10.sp) }
                        )
                        SuggestionChip(
                            onClick = { tempStr = "38.5"; humStr = "32.0"; moistureStr = "18.0" },
                            label = { Text("Heatwave/Drought", fontSize = 10.sp) }
                        )
                        SuggestionChip(
                            onClick = { tempStr = "14.0"; humStr = "92.0"; moistureStr = "85.0" },
                            label = { Text("Overwatered", fontSize = 10.sp) }
                        )
                    }

                    OutlinedTextField(
                        value = tempStr,
                        onValueChange = { tempStr = it },
                        label = { Text("Temperature (°C)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = humStr,
                        onValueChange = { humStr = it },
                        label = { Text("Humidity (% RH)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = moistureStr,
                        onValueChange = { moistureStr = it },
                        label = { Text("Soil Moisture (%)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val t = tempStr.toFloatOrNull() ?: 24f
                        val h = humStr.toFloatOrNull() ?: 60f
                        val m = moistureStr.toFloatOrNull() ?: 40f
                        viewModel.adminAddSensorReading(t, h, m)
                        showInjectDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GlowingEmerald)
                ) {
                    Text("INJECT RECORD", color = DeepForestBg, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showInjectDialog = false }) {
                    Text("CANCEL", color = TextMuted)
                }
            },
            containerColor = CardSurfaceDark
        )
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Clear All Sensor Logs", color = AlertCritical, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete all sensor telemetry logs?", color = TextLight) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.adminClearSensorData()
                        showClearConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AlertCritical)
                ) {
                    Text("CLEAR ALL", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("CANCEL", color = TextMuted)
                }
            },
            containerColor = CardSurfaceDark
        )
    }
}

// ==========================================
// 3. AI FOLIAR PREDICTIONS & SCANS TAB
// ==========================================
@Composable
fun AdminScansTab(
    predictions: List<Prediction>,
    plants: List<Plant>,
    viewModel: AgroViewModel
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingPrediction by remember { mutableStateOf<Prediction?>(null) }
    var showClearConfirm by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "AI SCANS & DIAGNOSES (${predictions.size})",
                color = GlowingEmerald,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedButton(
                    onClick = { showClearConfirm = true },
                    border = BorderStroke(1.dp, AlertCritical),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Clear All", color = AlertCritical, fontSize = 11.sp)
                }

                Button(
                    onClick = { showAddDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = GlowingEmerald),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add", tint = DeepForestBg, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Scan", color = DeepForestBg, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        }

        if (predictions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No foliar scan predictions recorded in Room database.", color = TextMuted)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(predictions, key = { it.id }) { pred ->
                    val plantName = plants.find { it.id == pred.plantId }?.name ?: "Crop #${pred.plantId}"
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                        border = BorderStroke(1.dp, SoftGrass),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = pred.predictedIssue,
                                        color = TextLight,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = "$plantName • ${SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(pred.timestamp))}",
                                        color = GlowingEmerald,
                                        fontSize = 11.sp
                                    )
                                }

                                Row {
                                    IconButton(
                                        onClick = { editingPrediction = pred },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = TextLight, modifier = Modifier.size(18.dp))
                                    }
                                    IconButton(
                                        onClick = { viewModel.adminDeletePrediction(pred.id) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = AlertCritical, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    text = "Health: ${pred.healthScore}%",
                                    color = if (pred.healthScore >= 70) GlowingEmerald else AlertWarning,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "Stress: ${pred.stressProbability}%",
                                    color = if (pred.stressProbability > 50) AlertCritical else TextMuted,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "Scan ID: #${pred.id}",
                                    color = TextMuted,
                                    fontSize = 12.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Advice: ${pred.recommendation}",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }

    // Add Prediction Dialog
    if (showAddDialog) {
        var selectedPlantId by remember { mutableStateOf(plants.firstOrNull()?.id ?: 1L) }
        var issue by remember { mutableStateOf("Nutrient Deficiency (Nitrogen Chlorosis)") }
        var healthScore by remember { mutableStateOf(68f) }
        var stressProb by remember { mutableStateOf(45f) }
        var advice by remember { mutableStateOf("Apply 10-5-5 organic foliar nitrogen spray in the early morning.") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Create AI Scan Record", color = TextLight, fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Target Crop:", color = TextMuted, fontSize = 12.sp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        plants.forEach { p ->
                            FilterChip(
                                selected = selectedPlantId == p.id,
                                onClick = { selectedPlantId = p.id },
                                label = { Text(p.name, fontSize = 11.sp) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = issue,
                        onValueChange = { issue = it },
                        label = { Text("Diagnosed Issue") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Health Score: ${healthScore.toInt()}%", color = GlowingEmerald, fontSize = 12.sp)
                    Slider(
                        value = healthScore,
                        onValueChange = { healthScore = it },
                        valueRange = 0f..100f
                    )

                    Text("Stress Probability: ${stressProb.toInt()}%", color = AlertWarning, fontSize = 12.sp)
                    Slider(
                        value = stressProb,
                        onValueChange = { stressProb = it },
                        valueRange = 0f..100f
                    )

                    OutlinedTextField(
                        value = advice,
                        onValueChange = { advice = it },
                        label = { Text("Recommendation") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.adminAddPrediction(
                            plantId = selectedPlantId,
                            predictedIssue = issue,
                            healthScore = healthScore.toInt(),
                            stressProbability = stressProb.toInt(),
                            recommendation = advice
                        )
                        showAddDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GlowingEmerald)
                ) {
                    Text("SAVE SCAN", color = DeepForestBg, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("CANCEL", color = TextMuted)
                }
            },
            containerColor = CardSurfaceDark
        )
    }

    // Edit Prediction Dialog
    editingPrediction?.let { pred ->
        var issue by remember { mutableStateOf(pred.predictedIssue) }
        var healthScore by remember { mutableStateOf(pred.healthScore.toFloat()) }
        var stressProb by remember { mutableStateOf(pred.stressProbability.toFloat()) }
        var advice by remember { mutableStateOf(pred.recommendation) }

        AlertDialog(
            onDismissRequest = { editingPrediction = null },
            title = { Text("Edit Scan #${pred.id}", color = TextLight, fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = issue,
                        onValueChange = { issue = it },
                        label = { Text("Diagnosed Issue") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Health Score: ${healthScore.toInt()}%", color = GlowingEmerald, fontSize = 12.sp)
                    Slider(
                        value = healthScore,
                        onValueChange = { healthScore = it },
                        valueRange = 0f..100f
                    )

                    Text("Stress Probability: ${stressProb.toInt()}%", color = AlertWarning, fontSize = 12.sp)
                    Slider(
                        value = stressProb,
                        onValueChange = { stressProb = it },
                        valueRange = 0f..100f
                    )

                    OutlinedTextField(
                        value = advice,
                        onValueChange = { advice = it },
                        label = { Text("Recommendation") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.adminUpdatePrediction(
                            pred.copy(
                                predictedIssue = issue,
                                healthScore = healthScore.toInt(),
                                stressProbability = stressProb.toInt(),
                                recommendation = advice
                            )
                        )
                        editingPrediction = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GlowingEmerald)
                ) {
                    Text("UPDATE", color = DeepForestBg, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingPrediction = null }) {
                    Text("CANCEL", color = TextMuted)
                }
            },
            containerColor = CardSurfaceDark
        )
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Clear All Predictions", color = AlertCritical, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to clear all foliar scans from the database?", color = TextLight) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.adminClearPredictions()
                        showClearConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AlertCritical)
                ) {
                    Text("CLEAR ALL", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("CANCEL", color = TextMuted)
                }
            },
            containerColor = CardSurfaceDark
        )
    }
}

// ==========================================
// 4. HEALTH HISTORY TIMELINE TAB
// ==========================================
@Composable
fun AdminHistoryTab(
    histories: List<HealthHistory>,
    plants: List<Plant>,
    viewModel: AgroViewModel
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingHistory by remember { mutableStateOf<HealthHistory?>(null) }
    var showClearConfirm by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "HEALTH TIMELINE (${histories.size})",
                color = GlowingEmerald,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedButton(
                    onClick = { showClearConfirm = true },
                    border = BorderStroke(1.dp, AlertCritical),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Clear All", color = AlertCritical, fontSize = 11.sp)
                }

                Button(
                    onClick = { showAddDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = GlowingEmerald),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add", tint = DeepForestBg, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Entry", color = DeepForestBg, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        }

        if (histories.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No physiological health timeline records.", color = TextMuted)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(histories, key = { it.id }) { item ->
                    val plantName = plants.find { it.id == item.plantId }?.name ?: "Crop #${item.plantId}"
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                        border = BorderStroke(1.dp, SoftGrass),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "$plantName • ${item.dateString}",
                                    color = TextLight,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text(
                                        text = "Health: ${item.healthScore}%",
                                        color = GlowingEmerald,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = "Stress: ${item.stressProbability}%",
                                        color = if (item.stressProbability > 50) AlertCritical else TextMuted,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = "ID: #${item.id}",
                                        color = TextMuted,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Row {
                                IconButton(
                                    onClick = { editingHistory = item },
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = TextLight, modifier = Modifier.size(16.dp))
                                }
                                IconButton(
                                    onClick = { viewModel.adminDeleteHealthHistory(item.id) },
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = AlertCritical, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add History Dialog
    if (showAddDialog) {
        var selectedPlantId by remember { mutableStateOf(plants.firstOrNull()?.id ?: 1L) }
        var dateString by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) }
        var healthScore by remember { mutableStateOf(85f) }
        var stressProb by remember { mutableStateOf(20f) }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Health History Record", color = TextLight, fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Crop:", color = TextMuted, fontSize = 12.sp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        plants.forEach { p ->
                            FilterChip(
                                selected = selectedPlantId == p.id,
                                onClick = { selectedPlantId = p.id },
                                label = { Text(p.name, fontSize = 11.sp) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = dateString,
                        onValueChange = { dateString = it },
                        label = { Text("Date (YYYY-MM-DD)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Health Score: ${healthScore.toInt()}%", color = GlowingEmerald, fontSize = 12.sp)
                    Slider(
                        value = healthScore,
                        onValueChange = { healthScore = it },
                        valueRange = 0f..100f
                    )

                    Text("Stress Probability: ${stressProb.toInt()}%", color = AlertWarning, fontSize = 12.sp)
                    Slider(
                        value = stressProb,
                        onValueChange = { stressProb = it },
                        valueRange = 0f..100f
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.adminAddHealthHistory(
                            plantId = selectedPlantId,
                            dateString = dateString,
                            healthScore = healthScore.toInt(),
                            stressProbability = stressProb.toInt()
                        )
                        showAddDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GlowingEmerald)
                ) {
                    Text("ADD RECORD", color = DeepForestBg, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("CANCEL", color = TextMuted)
                }
            },
            containerColor = CardSurfaceDark
        )
    }

    // Edit History Dialog
    editingHistory?.let { hist ->
        var dateString by remember { mutableStateOf(hist.dateString) }
        var healthScore by remember { mutableStateOf(hist.healthScore.toFloat()) }
        var stressProb by remember { mutableStateOf(hist.stressProbability.toFloat()) }

        AlertDialog(
            onDismissRequest = { editingHistory = null },
            title = { Text("Edit History #${hist.id}", color = TextLight, fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = dateString,
                        onValueChange = { dateString = it },
                        label = { Text("Date (YYYY-MM-DD)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Health Score: ${healthScore.toInt()}%", color = GlowingEmerald, fontSize = 12.sp)
                    Slider(
                        value = healthScore,
                        onValueChange = { healthScore = it },
                        valueRange = 0f..100f
                    )

                    Text("Stress Probability: ${stressProb.toInt()}%", color = AlertWarning, fontSize = 12.sp)
                    Slider(
                        value = stressProb,
                        onValueChange = { stressProb = it },
                        valueRange = 0f..100f
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.adminUpdateHealthHistory(
                            hist.copy(
                                dateString = dateString,
                                healthScore = healthScore.toInt(),
                                stressProbability = stressProb.toInt()
                            )
                        )
                        editingHistory = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GlowingEmerald)
                ) {
                    Text("UPDATE", color = DeepForestBg, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingHistory = null }) {
                    Text("CANCEL", color = TextMuted)
                }
            },
            containerColor = CardSurfaceDark
        )
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Clear All History", color = AlertCritical, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to clear all historical health tracking entries?", color = TextLight) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.adminClearHealthHistory()
                        showClearConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AlertCritical)
                ) {
                    Text("CLEAR ALL", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("CANCEL", color = TextMuted)
                }
            },
            containerColor = CardSurfaceDark
        )
    }
}

// ==========================================
// 5. ALERTS & BROADCAST TAB
// ==========================================
@Composable
fun AdminAlertsTab(
    alerts: List<PlantAlert>,
    viewModel: AgroViewModel
) {
    var showBroadcastDialog by remember { mutableStateOf(false) }
    var showClearConfirm by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SYSTEM ALERTS (${alerts.size})",
                color = GlowingEmerald,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedButton(
                    onClick = { showClearConfirm = true },
                    border = BorderStroke(1.dp, AlertCritical),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Clear", color = AlertCritical, fontSize = 11.sp)
                }

                Button(
                    onClick = { showBroadcastDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = GlowingEmerald),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Filled.AddAlert, contentDescription = "Broadcast", tint = DeepForestBg, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Broadcast", color = DeepForestBg, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        }

        if (alerts.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No active system alerts in database.", color = TextMuted)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(alerts, key = { it.id }) { alert ->
                    val color = when (alert.severity) {
                        "CRITICAL" -> AlertCritical
                        "WARNING" -> AlertWarning
                        else -> GlowingEmerald
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                        border = BorderStroke(1.dp, color.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = color.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = alert.severity,
                                            color = color,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (alert.isRead) "Read" else "Unread",
                                        color = if (alert.isRead) TextMuted else AlertWarning,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = alert.message,
                                    color = TextLight,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault()).format(Date(alert.timestamp)) + " • ID: #${alert.id}",
                                    color = TextMuted,
                                    fontSize = 10.sp
                                )
                            }

                            IconButton(
                                onClick = { viewModel.adminDeleteAlert(alert.id) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = AlertCritical, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // Broadcast Alert Dialog
    if (showBroadcastDialog) {
        var message by remember { mutableStateOf("") }
        var severity by remember { mutableStateOf("WARNING") }

        AlertDialog(
            onDismissRequest = { showBroadcastDialog = false },
            title = { Text("Broadcast Agronomic Alert", color = TextLight, fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Severity:", color = TextMuted, fontSize = 12.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("INFO", "WARNING", "CRITICAL").forEach { s ->
                            FilterChip(
                                selected = severity == s,
                                onClick = { severity = s },
                                label = { Text(s, fontSize = 11.sp) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = message,
                        onValueChange = { message = it },
                        label = { Text("Alert Message") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (message.isNotBlank()) {
                            viewModel.adminCreateAlert(message, severity)
                            showBroadcastDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GlowingEmerald)
                ) {
                    Text("BROADCAST", color = DeepForestBg, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBroadcastDialog = false }) {
                    Text("CANCEL", color = TextMuted)
                }
            },
            containerColor = CardSurfaceDark
        )
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Clear All Alerts", color = AlertCritical, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to clear all active alerts from database?", color = TextLight) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.adminClearAlerts()
                        showClearConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AlertCritical)
                ) {
                    Text("CLEAR ALL", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("CANCEL", color = TextMuted)
                }
            },
            containerColor = CardSurfaceDark
        )
    }
}

// ==========================================
// 6. FEEDBACK & REVIEWS TAB
// ==========================================
@Composable
fun AdminFeedbackTab(
    feedbackList: List<ScanFeedback>,
    viewModel: AgroViewModel
) {
    var editingFeedback by remember { mutableStateOf<ScanFeedback?>(null) }
    var showClearConfirm by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "FARMER REVIEWS (${feedbackList.size})",
                color = GlowingEmerald,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )

            OutlinedButton(
                onClick = { showClearConfirm = true },
                border = BorderStroke(1.dp, AlertCritical),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Clear All", color = AlertCritical, fontSize = 11.sp)
            }
        }

        if (feedbackList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No user/agronomist feedback entries in database.", color = TextMuted)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(feedbackList, key = { it.id }) { fb ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                        border = BorderStroke(1.dp, SoftGrass),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${fb.plantName} • ${fb.predictedIssue}",
                                        color = TextLight,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "By ${fb.userEmail} • ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(fb.timestamp))}",
                                        color = GlowingEmerald,
                                        fontSize = 11.sp
                                    )
                                }

                                Row {
                                    IconButton(
                                        onClick = { editingFeedback = fb },
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = TextLight, modifier = Modifier.size(16.dp))
                                    }
                                    IconButton(
                                        onClick = { viewModel.adminDeleteFeedback(fb.id) },
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = AlertCritical, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Text(
                                    text = "Prediction: ★ ${fb.predictionAccuracyRating}/5",
                                    color = AlertWarning,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "Advice: ★ ${fb.recommendationAccuracyRating}/5",
                                    color = AlertWarning,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }

                            if (fb.comments.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "\"${fb.comments}\"",
                                    color = TextMuted,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Edit Feedback Dialog
    editingFeedback?.let { fb ->
        var predRating by remember { mutableStateOf(fb.predictionAccuracyRating) }
        var recRating by remember { mutableStateOf(fb.recommendationAccuracyRating) }
        var comments by remember { mutableStateOf(fb.comments) }

        AlertDialog(
            onDismissRequest = { editingFeedback = null },
            title = { Text("Edit Review #${fb.id}", color = TextLight, fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Prediction Accuracy (Stars: $predRating):", color = TextMuted, fontSize = 12.sp)
                    Row {
                        (1..5).forEach { star ->
                            IconButton(onClick = { predRating = star }, modifier = Modifier.size(36.dp)) {
                                Icon(
                                    imageVector = if (star <= predRating) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                    contentDescription = "$star",
                                    tint = AlertWarning
                                )
                            }
                        }
                    }

                    Text("Advice Quality (Stars: $recRating):", color = TextMuted, fontSize = 12.sp)
                    Row {
                        (1..5).forEach { star ->
                            IconButton(onClick = { recRating = star }, modifier = Modifier.size(36.dp)) {
                                Icon(
                                    imageVector = if (star <= recRating) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                    contentDescription = "$star",
                                    tint = AlertWarning
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = comments,
                        onValueChange = { comments = it },
                        label = { Text("Agronomist Comments") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.adminUpdateFeedback(
                            fb.copy(
                                predictionAccuracyRating = predRating,
                                recommendationAccuracyRating = recRating,
                                comments = comments
                            )
                        )
                        editingFeedback = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GlowingEmerald)
                ) {
                    Text("UPDATE", color = DeepForestBg, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingFeedback = null }) {
                    Text("CANCEL", color = TextMuted)
                }
            },
            containerColor = CardSurfaceDark
        )
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Clear All Feedback", color = AlertCritical, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to clear all feedback entries from database?", color = TextLight) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.adminClearFeedback()
                        showClearConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AlertCritical)
                ) {
                    Text("CLEAR ALL", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("CANCEL", color = TextMuted)
                }
            },
            containerColor = CardSurfaceDark
        )
    }
}

// ==========================================
// 7. DATABASE MAINTENANCE & SYSTEM CONFIG TAB
// ==========================================
@Composable
fun AdminMaintenanceTab(
    plants: List<Plant>,
    sensors: List<SensorData>,
    predictions: List<Prediction>,
    histories: List<HealthHistory>,
    alerts: List<PlantAlert>,
    feedbackList: List<ScanFeedback>,
    viewModel: AgroViewModel
) {
    var showResetDefaultsConfirm by remember { mutableStateOf(false) }
    var showWipeConfirm by remember { mutableStateOf(false) }

    var fastApiUrlInput by remember { mutableStateOf(viewModel.fastApiUrl) }
    var soilMoistureThresholdInput by remember { mutableStateOf(viewModel.soilMoistureThreshold.toString()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "DATABASE INVENTORY & SCHEMA STATUS",
            color = GlowingEmerald,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )

        // Database Table Inventory Matrix
        Card(
            colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
            border = BorderStroke(1.dp, SoftGrass),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Room SQLite: agrosense_database",
                    color = TextLight,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    DatabaseStatItem(title = "plants", count = plants.size)
                    DatabaseStatItem(title = "sensor_data", count = sensors.size)
                    DatabaseStatItem(title = "predictions", count = predictions.size)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    DatabaseStatItem(title = "health_history", count = histories.size)
                    DatabaseStatItem(title = "alerts", count = alerts.size)
                    DatabaseStatItem(title = "feedback", count = feedbackList.size)
                }
            }
        }

        // Global Threshold & API Settings
        Card(
            colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
            border = BorderStroke(1.dp, SoftGrass),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "APPLICATION CONFIGURATION",
                    color = GlowingEmerald,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = fastApiUrlInput,
                    onValueChange = {
                        fastApiUrlInput = it
                        viewModel.fastApiUrl = it
                    },
                    label = { Text("FastAPI Endpoint URL") },
                    placeholder = { Text("https://my-fastapi-backend.run.app") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = soilMoistureThresholdInput,
                    onValueChange = {
                        soilMoistureThresholdInput = it
                        it.toFloatOrNull()?.let { f -> viewModel.soilMoistureThreshold = f }
                    },
                    label = { Text("Soil Moisture Alert Threshold (%)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Database Reset / Wipe Operations
        Card(
            colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
            border = BorderStroke(1.dp, AlertWarning.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "DANGER ZONE & RESTORE",
                    color = AlertWarning,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Restore application data to initial demonstration dataset or wipe local tables entirely.",
                    color = TextMuted,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = { showResetDefaultsConfirm = true },
                    colors = ButtonDefaults.buttonColors(containerColor = GlowingEmerald),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_reset_defaults_btn")
                ) {
                    Icon(Icons.Filled.Restore, contentDescription = "Restore", tint = DeepForestBg)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("RESTORE DEFAULT SEED DATASET", color = DeepForestBg, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { showWipeConfirm = true },
                    colors = ButtonDefaults.buttonColors(containerColor = AlertCritical),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_wipe_db_btn")
                ) {
                    Icon(Icons.Filled.DeleteForever, contentDescription = "Wipe", tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("WIPE ENTIRE DATABASE (CLEAR ALL)", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showResetDefaultsConfirm) {
        AlertDialog(
            onDismissRequest = { showResetDefaultsConfirm = false },
            title = { Text("Restore Default Seed Data", color = GlowingEmerald, fontWeight = FontWeight.Bold) },
            text = { Text("This will reset all tables (plants, sensors, predictions, history, alerts, feedback) and seed clean initial demo data. Continue?", color = TextLight) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.adminResetDatabaseToDefaults()
                        showResetDefaultsConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GlowingEmerald)
                ) {
                    Text("CONFIRM RESTORE", color = DeepForestBg, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDefaultsConfirm = false }) {
                    Text("CANCEL", color = TextMuted)
                }
            },
            containerColor = CardSurfaceDark
        )
    }

    if (showWipeConfirm) {
        AlertDialog(
            onDismissRequest = { showWipeConfirm = false },
            title = { Text("Wipe Entire Database", color = AlertCritical, fontWeight = FontWeight.Bold) },
            text = { Text("WARNING: All data across all 6 tables will be permanently erased. Are you completely sure?", color = TextLight) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.adminWipeEntireDatabase()
                        showWipeConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AlertCritical)
                ) {
                    Text("CONFIRM WIPE", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showWipeConfirm = false }) {
                    Text("CANCEL", color = TextMuted)
                }
            },
            containerColor = CardSurfaceDark
        )
    }
}

@Composable
fun DatabaseStatItem(title: String, count: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count.toString(),
            color = GlowingEmerald,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        Text(
            text = title,
            color = TextMuted,
            fontSize = 10.sp
        )
    }
}
