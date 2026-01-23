using Newtonsoft.Json;
using System.IO;

namespace GlideDeckReceiver;

public class MacroItem
{
    public string Id { get; set; } = Guid.NewGuid().ToString();
    public string Name { get; set; } = "New Macro";
    public List<VirtualKey> Keys { get; set; } = new List<VirtualKey>();
}

public class MacroManager
{

    public List<MacroItem> Macros { get; private set; } = new List<MacroItem>();

    private string _filePath;

    public MacroManager() 
    {
        var appData = Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData);
        var dir = Path.Combine(appData, "GlideDeckReceiver");
        Directory.CreateDirectory(dir);
        _filePath = Path.Combine(dir, "macros.json");
        Load();
    }

    public void Load()
    {
        try
        {
            if (File.Exists(_filePath))
            {
                var json = File.ReadAllText(_filePath);
                Macros = JsonConvert.DeserializeObject<List<MacroItem>>(json) ?? new List<MacroItem>();
            }
        }
        catch
        {
            Macros = new List<MacroItem>();
        }
    }

    public void Save()
    {
        try
        {
            var json = JsonConvert.SerializeObject(Macros, Formatting.Indented);
            File.WriteAllText(_filePath, json);
        }
        catch
        {
            // Ignore save errors
        }
    }

    public void AddMacro(MacroItem macro)
    {
        Macros.Add(macro);
        Save();
    }

    public void RemoveMacro(string id)
    {
        Macros.RemoveAll(m => m.Id == id);
        Save();
    }

    public void executeMacro(string id)
    {
        var macro = Macros.FirstOrDefault(m => m.Id == id);
        if (macro != null && macro.Keys.Count > 0)
        {
            // Execute keys
            // Logic: If multiple keys, treat as combination (hold all, then release all).
            // Unless we want sequential. Requirement says "Input Macro Keys", usually implies combination or sequence.
            // For simple implementation, let's assume combination if < 4 keys, or sequence if many?
            // "Macro" usually implies sequence in many gaming mice, but "Shortcuts" (Ctrl+C) are combinations.
            // The user request says "Macro keys input... press button... keys pressed".
            // Let's support both implicitly or stick to combination for now as that's most useful for remote control (Ctrl+C, Ctrl+V).
            
            // Standard approach for simple macros: Hold all modifiers, tap the final key?
            // Whatever is in the list, we press all down, then all up in reverse.
            
            foreach (var key in macro.Keys)
            {
                InputSynthesizer.KeyDown(key);
                Thread.Sleep(10); // Check stability
            }

            foreach (var key in ((IEnumerable<VirtualKey>)macro.Keys).Reverse())
            {
                InputSynthesizer.KeyUp(key);
                Thread.Sleep(10);
            }
        }
    }
}

