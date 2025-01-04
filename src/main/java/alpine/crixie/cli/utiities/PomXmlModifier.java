package alpine.crixie.cli.utiities;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.InvalidParameterException;

public class PomXmlModifier {
    private String jarName;

    public  PomXmlModifier jarFileName(String jarFileName){
        if(jarFileName.trim().isEmpty()){
            throw new InvalidParameterException("jar file name can not be null or empty");
        }
        this.jarName = jarFileName;
        return this;
    }
    
    public void modify() {
        String jarFilePath = "cryxie_libs/" + jarName;

        try {
            // Lê o POM existente
            File pomFile = new File("pom.xml");
            SAXBuilder saxBuilder = new SAXBuilder();
            Document document = saxBuilder.build(pomFile);

            // Obtém o elemento raiz
            Element projectElement = document.getRootElement();

            Element dependenciesElement = getDependenciesTag(projectElement);
            // Adiciona uma quebra de linha antes, se necessário
            dependenciesElement.addContent("\n    ");

            // Adiciona a dependência à lista de dependências
            dependenciesElement.addContent(addNewDependency(projectElement, jarName, jarFilePath));

            // Adiciona uma quebra de linha após a dependência
            dependenciesElement.addContent("\n");

            saveModifications(document);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Element getDependenciesTag(Element projectElement) {
        // Localiza ou cria a tag <dependencies>
        Element dependenciesElement = projectElement.getChild("dependencies", projectElement.getNamespace());
        if (dependenciesElement == null) {
            dependenciesElement = new Element("dependencies", projectElement.getNamespace());
            projectElement.addContent(dependenciesElement);
        }
        return dependenciesElement;
    }

    private static void saveModifications(Document document) throws IOException {
        // Salva o POM atualizado com formatação
        Format format = Format.getPrettyFormat();
        format.setIndent("  "); // Define a indentação como dois espaços
        format.setLineSeparator(System.lineSeparator()); // Adiciona separação de linha nativa do sistema

        XMLOutputter outputter = new XMLOutputter(format);
        try (FileWriter writer = new FileWriter("pom.xml")) {
            outputter.output(document, writer);
        }
    }

    private static Element addNewDependency(Element projectElement, String jarName, String jarFilePath) {
        // Cria a nova dependência
        Element dependency = new Element("dependency", projectElement.getNamespace());

        Element groupId = new Element("groupId", projectElement.getNamespace()).setText(jarName);
        Element artifactId = new Element("artifactId", projectElement.getNamespace()).setText(jarName);
        Element version = new Element("version", projectElement.getNamespace()).setText("not-provided");
        Element scope = new Element("scope", projectElement.getNamespace()).setText("system");
        Element systemPath = new Element("systemPath", projectElement.getNamespace())
                .setText("${project.basedir}/" + jarFilePath);

        // Adiciona os elementos à dependência
        dependency.addContent(groupId);
        dependency.addContent(artifactId);
        dependency.addContent(version);
        dependency.addContent(scope);
        dependency.addContent(systemPath);
        return dependency;
    }
}