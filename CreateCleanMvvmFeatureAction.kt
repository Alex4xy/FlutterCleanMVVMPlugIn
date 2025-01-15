package com.clean_mvvm.clean_mvvm_feature_directories

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import java.io.File
import javax.swing.JOptionPane

class CreateCleanMvvmFeatureAction : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        // Get the current project and selected directory
        val project: Project? = event.project
        val selectedFile: VirtualFile? =
            event.dataContext.getData(com.intellij.openapi.actionSystem.CommonDataKeys.VIRTUAL_FILE)

        if (project == null || selectedFile == null) {
            return
        }

        // Prompt the user to enter the feature name
        val featureName = JOptionPane.showInputDialog("Enter Feature Name:") ?: return
        if (featureName.isBlank()) {
            return
        }

        val appName = JOptionPane.showInputDialog("Enter Application Name:") ?: return
        if (appName.isBlank()) {
            return
        }

        // Create the directory structure under the selected directory
        val basePath = "${selectedFile.path}/${featureName.toSnakeCase()}"
        createDirectoryStructure(basePath, featureName, appName)

        // Refresh the project view to reflect the changes
        selectedFile.refresh(false, true)
    }

    private fun createDirectoryStructure(basePath: String, featureName: String, appName: String) {
        val directories = listOf(
            "data/data_store",
            "data/entities",
            "data/network",
            "data/repositories",
            "domain/entities",
            "domain/repository",
            "domain/usecases",
            "presentation/event",
            "presentation/state",
            "presentation/viewmodel",
            "presentation/widgets"
        )

        directories.forEach { dir ->
            File("$basePath/$dir").mkdirs()
        }

        // Generate template classes
        generateTemplateClasses(basePath, featureName, appName)
    }

    private fun generateTemplateClasses(basePath: String, featureName: String, appNameFromSet: String) {
        val snakeCaseFeatureName = featureName.toSnakeCase()
        val pascalCaseFeatureName = featureName.capitalize()
        val appName = appNameFromSet.lowercase()

        // ==============================
        // DATA: Data Store Class
        // ==============================
        val dataStoreContent = """
        import 'package:shared_preferences/shared_preferences.dart';

        //TODO: Register data store in dependency 
        class ${pascalCaseFeatureName}DataStore {
        final SharedPreferences _prefs;

        ${pascalCaseFeatureName}DataStore(this._prefs);

        Future<void> setBool(String key, bool value) async {
                await _prefs.setBool(key, value);
        }

        bool? getBool(String key) {
                return _prefs.getBool(key);
        }

        Future<void> setString(String key, String value) async {
                await _prefs.setString(key, value);
        }

        String? getString(String key) {
                return _prefs.getString(key);
        }

        Future<void> setInt(String key, int value) async {
                await _prefs.setInt(key, value);
        }

        int? getInt(String key) {
                return _prefs.getInt(key);
        }

        Future<void> setDouble(String key, double value) async {
                await _prefs.setDouble(key, value);
        }

        double? getDouble(String key) {
                return _prefs.getDouble(key);
        }

        //======================================
        // Never save large list. Use with caution and only if needed. Large list in preferences can cause performance issues
        // Use database if object or large list needs to be saved
        //======================================
        Future<void> setStringList(String key, List<String> value) async {
                await _prefs.setStringList(key, value);
        }

        List<String>? getStringList(String key) {
                return _prefs.getStringList(key);
          }
        }
        
    """.trimIndent()
        File("$basePath/data/data_store/${snakeCaseFeatureName}_data_store.dart").writeText(dataStoreContent)

        // ==============================
        // DATA: Request Class
        // ==============================
        val requestContent = """
        import 'package:json_annotation/json_annotation.dart';

        part '${snakeCaseFeatureName}_request.g.dart';

        @JsonSerializable()
        class ${pascalCaseFeatureName}Request {
            final int id;

            ${pascalCaseFeatureName}Request({required this.id});

            factory ${pascalCaseFeatureName}Request.fromJson(Map<String, dynamic> json) => _$${pascalCaseFeatureName}RequestFromJson(json);
            Map<String, dynamic> toJson() => _$${pascalCaseFeatureName}RequestToJson(this);
        }
    """.trimIndent()
        File("$basePath/data/entities/${snakeCaseFeatureName}_request.dart").writeText(requestContent)

        // ==============================
        // DATA: Response Class
        // ==============================
        val responseContent = """
        import 'package:${appName}/features/${snakeCaseFeatureName}/domain/entities/${snakeCaseFeatureName}_model.dart';
        import 'package:json_annotation/json_annotation.dart';

        part '${snakeCaseFeatureName}_response.g.dart';

        @JsonSerializable()
        class ${pascalCaseFeatureName}Response {
            final int id;

            ${pascalCaseFeatureName}Response({required this.id});

            factory ${pascalCaseFeatureName}Response.fromJson(Map<String, dynamic> json) => _$${pascalCaseFeatureName}ResponseFromJson(json);

            Map<String, dynamic> toJson() => _$${pascalCaseFeatureName}ResponseToJson(this);

            ${pascalCaseFeatureName}Model toDomain() {
                return ${pascalCaseFeatureName}Model(id: id);
            }
        }
    """.trimIndent()
        File("$basePath/data/entities/${snakeCaseFeatureName}_response.dart").writeText(responseContent)

        // ==============================
        // DATA: API Class
        // ==============================
        val apiContent = """
        import 'package:dio/dio.dart';

        //TODO: Register class in Api Module
        class ${pascalCaseFeatureName}Api {
            final Dio _dio;

            ${pascalCaseFeatureName}Api(this._dio);

            Future<Response> get${pascalCaseFeatureName}Data() async {
                return await _dio.get('/${snakeCaseFeatureName}');
            }
        }
    """.trimIndent()
        File("$basePath/data/network/${snakeCaseFeatureName}_api.dart").writeText(apiContent)

        // ==============================
        // DATA: Repository Implementation Class
        // ==============================
        val repositoryImplContent = """
        import 'package:${appName}/features/${snakeCaseFeatureName}/domain/entities/${snakeCaseFeatureName}_model.dart';
        import 'package:${appName}/features/${snakeCaseFeatureName}/data/entities/${snakeCaseFeatureName}_response.dart';
        import 'package:${appName}/features/${snakeCaseFeatureName}/data/network/${snakeCaseFeatureName}_api.dart';
        import 'package:${appName}/features/${snakeCaseFeatureName}/domain/repository/${snakeCaseFeatureName}_repository.dart';

        class ${pascalCaseFeatureName}RepositoryImpl implements ${pascalCaseFeatureName}Repository {
            final ${pascalCaseFeatureName}Api _api;

            ${pascalCaseFeatureName}RepositoryImpl(this._api);

            @override
            Future<${pascalCaseFeatureName}Model> get${pascalCaseFeatureName}() async {
                final response = await _api.get${pascalCaseFeatureName}Data();
                final ${snakeCaseFeatureName}Response = ${pascalCaseFeatureName}Response.fromJson(response.data);
                return ${snakeCaseFeatureName}Response.toDomain();
            }
        }
    """.trimIndent()
        File("$basePath/data/repositories/${snakeCaseFeatureName}_repository_impl.dart").writeText(repositoryImplContent)

        // ==============================
        // DOMAIN: Entity Class
        // ==============================
        val entityContent = """
        
        class ${pascalCaseFeatureName}Model {
         // TODO: Define properties here. Example:
         final int id;

        ${pascalCaseFeatureName}Model({
          // TODO: Define properties here. Example:
          required this.id,
          });
        }

    """.trimIndent()
        File("$basePath/domain/entities/${snakeCaseFeatureName}_model.dart").writeText(entityContent)

        // ==============================
        // DOMAIN: Repository Class
        // ==============================
        val repositoryContent = """
        import 'package:$appName/features/$snakeCaseFeatureName/domain/entities/${snakeCaseFeatureName}_model.dart';
        
        //TODO: Must register Repository class in DI  
        abstract class ${pascalCaseFeatureName}Repository {
            Future<${pascalCaseFeatureName}Model> get${pascalCaseFeatureName}();
        }
    """.trimIndent()
        File("$basePath/domain/repository/${snakeCaseFeatureName}_repository.dart").writeText(repositoryContent)

        // ==============================
        // DOMAIN: Use Case Class
        // ==============================
        val useCaseContent = """
        import 'package:$appName/features/${snakeCaseFeatureName}/domain/repository/${snakeCaseFeatureName}_repository.dart';
        import 'package:$appName/features/${snakeCaseFeatureName}/domain/entities/${snakeCaseFeatureName}_model.dart';
        
        //TODO: Must register UseCase class in DI 
        class ${pascalCaseFeatureName}UseCase {
            final ${pascalCaseFeatureName}Repository _repository;

            ${pascalCaseFeatureName}UseCase(this._repository);

            Future<${pascalCaseFeatureName}Model> execute() {
                return _repository.get${pascalCaseFeatureName}();
            }
        }
    """.trimIndent()
        File("$basePath/domain/usecases/${snakeCaseFeatureName}_usecase.dart").writeText(useCaseContent)


        // ==============================
        // PRESENTATION: Event Class
        // ==============================
        val eventClassContent = """
        import 'package:$appName/features/${snakeCaseFeatureName}/domain/entities/${snakeCaseFeatureName}_model.dart';

        abstract class ${pascalCaseFeatureName}Event {}

        class Load${pascalCaseFeatureName}Data extends ${pascalCaseFeatureName}Event {}

        class Refresh${pascalCaseFeatureName}Data extends ${pascalCaseFeatureName}Event {}

        //==========================
        // NOTE: Implement if navigation is needed otherwise remove
        //==========================
        /*
        class NavigateToScreen(Your navigation screen name) extends SettingsEvent {
        //Optional: Set values or even model in the constructor if need arise.
        final String title;

         NavigateTo(Your navigation screen name)(this.title);
            }
        }*/
    """.trimIndent()
        File("$basePath/presentation/event/${snakeCaseFeatureName}_event.dart").writeText(eventClassContent)

        // ==============================
        // PRESENTATION: UI State Class
        // ==============================
        val stateContent = """
        import 'package:$appName/features/${snakeCaseFeatureName}/domain/entities/${snakeCaseFeatureName}_model.dart';
        
        abstract class ${pascalCaseFeatureName}UiState {}

        class ${pascalCaseFeatureName}Loading extends ${pascalCaseFeatureName}UiState {}

        class ${pascalCaseFeatureName}Success extends ${pascalCaseFeatureName}UiState {

              final ${pascalCaseFeatureName}Model ${snakeCaseFeatureName}Model;

              ${pascalCaseFeatureName}Success(this.${snakeCaseFeatureName}Model);
        }

        class ${pascalCaseFeatureName}Error extends ${pascalCaseFeatureName}UiState {
            final String message;

            ${pascalCaseFeatureName}Error(this.message);
        }
    """.trimIndent()
        File("$basePath/presentation/state/${snakeCaseFeatureName}_ui_state.dart").writeText(stateContent)

        // ==============================
        // PRESENTATION: ViewModel Class
        // ==============================
        val viewModelContent = """
        import 'package:$appName/features/${snakeCaseFeatureName}/data/data_store/${snakeCaseFeatureName}_data_store.dart';
        import 'package:$appName/features/${snakeCaseFeatureName}/domain/usecases/${snakeCaseFeatureName}_usecase.dart';
        import 'package:$appName/features/${snakeCaseFeatureName}/presentation/event/${snakeCaseFeatureName}_event.dart';
        import 'package:$appName/features/${snakeCaseFeatureName}/presentation/state/${snakeCaseFeatureName}_ui_state.dart';
        import 'package:flutter/cupertino.dart';
        import 'package:rxdart/rxdart.dart';

        class ${pascalCaseFeatureName}ViewModel extends ChangeNotifier {
                final ${pascalCaseFeatureName}UseCase _useCase;
                final ${pascalCaseFeatureName}DataStore _dataStore;

                // UI State management
                final _stateSubject = BehaviorSubject<${pascalCaseFeatureName}UiState>.seeded(${pascalCaseFeatureName}Loading());
                Stream<${pascalCaseFeatureName}UiState> get stateStream => _stateSubject.stream;

                // Navigation management
                final _navigationSubject = BehaviorSubject<Function(BuildContext)>();
                Stream<Function(BuildContext)> get navigationStream => _navigationSubject.stream;

                ${pascalCaseFeatureName}ViewModel(this._useCase, this._dataStore);

                void handleEvent(${pascalCaseFeatureName}Event event) {
                        switch (event) {
                                case Load${pascalCaseFeatureName}Data():
                                        _initData();
                                        break;
                                case Refresh${pascalCaseFeatureName}Data():
                                        _initData();
                                        break;
                                default:
                                        // Handle unknown events or do nothing
                                        break;
                        }
                }

                void _initData() {
                        _stateSubject.add(${pascalCaseFeatureName}Loading());
                        _useCase.execute().then((result) {
                                _stateSubject.add(${pascalCaseFeatureName}Success(result));
                        }).catchError((error) {
                                _stateSubject.add(${pascalCaseFeatureName}Error("Failed to load ${snakeCaseFeatureName}"));
                        });
                }

                void _navigateTo(Function(BuildContext) builder) {
                        _navigationSubject.add(builder);
                }

                @override
                void dispose() {
                        _stateSubject.close();
                        _navigationSubject.close();
                        super.dispose();
                }
        }
    """.trimIndent()
    File("$basePath/presentation/viewmodel/${snakeCaseFeatureName}_view_model.dart").writeText(viewModelContent)


        // ==============================
        // PRESENTATION: Screen Widget Class
        // ==============================
        val screenContent = """
        import 'package:$appName/core/resources/app_strings.dart';
        import 'package:$appName/core/widgets/error_widget.dart';
        import 'package:$appName/core/widgets/loading_widget.dart';
        import 'package:$appName/features/${snakeCaseFeatureName}/presentation/event/${snakeCaseFeatureName}_event.dart';
        import 'package:$appName/features/${snakeCaseFeatureName}/presentation/state/${snakeCaseFeatureName}_ui_state.dart';
        import 'package:$appName/features/${snakeCaseFeatureName}/presentation/viewmodel/${snakeCaseFeatureName}_view_model.dart';
        import 'package:$appName/features/${snakeCaseFeatureName}/presentation/widgets/${snakeCaseFeatureName}_screen_content.dart';
        import 'package:flutter/material.dart';
        import 'package:get_it/get_it.dart';

        class ${pascalCaseFeatureName}Screen extends StatefulWidget {
            const ${pascalCaseFeatureName}Screen({super.key, required this.title});

            final String title;

            @override
            State<${pascalCaseFeatureName}Screen> createState() => _${pascalCaseFeatureName}ScreenState();
        }

        class _${pascalCaseFeatureName}ScreenState extends State<${pascalCaseFeatureName}Screen> {
            final viewModel = GetIt.instance<${pascalCaseFeatureName}ViewModel>();

            @override
            void initState() {
                super.initState();
                    viewModel.handleEvent(Load${pascalCaseFeatureName}Data());
            }

            @override
            Widget build(BuildContext context) {
                return Scaffold(
                    appBar: AppBar(
                        title: Text(widget.title),
                        actions: [
                            IconButton(
                                icon: Icon(Icons.settings),
                                onPressed: () {
                                    // TODO: Implement action
                                },
                            ),
                        ],
                        backgroundColor: Colors.deepOrange, // Set the background color
                        elevation: 4.0, // Set elevation for a shadow effect
                    ),
                    body: StreamBuilder<${pascalCaseFeatureName}UiState>(
                        stream: viewModel.stateStream,
                        builder: (context, snapshot) {
                            final state = snapshot.data;
                            if (state is ${pascalCaseFeatureName}Loading) {
                                return loadingWidget();
                            } else if (state is ${pascalCaseFeatureName}Success) {
                                return SingleChildScrollView(
                                    child: ${snakeCaseFeatureName}ScreenContent(),
                                );
                                // Or use ListView if needed for nested scroll or long list
                                /* return ListView(
                                    children: [${snakeCaseFeatureName}ScreenContent()],
                                ); */
                            } else if (state is ${pascalCaseFeatureName}Error) {
                                return errorWidget(state.message);
                            } else {
                                return Center(child: Text(AppStrings.errorState));
                            }
                        },
                    ),
                );
            }
        }
    """.trimIndent()
        File("$basePath/presentation/widgets/${snakeCaseFeatureName}_screen.dart").writeText(screenContent)

        // ==============================
        // PRESENTATION: Screen Content Widget
        // ==============================
        val screenWidgetContent = """
        import 'package:flutter/material.dart';

        Widget ${snakeCaseFeatureName}ScreenContent() {
            // TODO: Implement your screen
            return Column();
        }
    """.trimIndent()
        File("$basePath/presentation/widgets/${snakeCaseFeatureName}_screen_content.dart").writeText(screenWidgetContent)


    }

    // Extension function to convert a string to snake_case
    private fun String.toSnakeCase(): String {
        return this.replace(Regex("([a-z])([A-Z]+)"), "$1_$2")
            .replace(" ", "_")
            .lowercase()
    }

    // Extension function to capitalize the first letter (for PascalCase)
    private fun String.capitalize(): String {
        return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }
}