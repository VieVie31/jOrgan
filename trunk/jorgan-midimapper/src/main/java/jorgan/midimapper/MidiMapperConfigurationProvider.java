package jorgan.midimapper;

import java.util.ArrayList;
import java.util.List;

import bias.Store;
import bias.store.DefaultingStore;
import bias.store.PreferencesStore;
import bias.store.PropertiesStore;

public class MidiMapperConfigurationProvider {

	public List<Store> getStores() {
		ArrayList<Store> stores = new ArrayList<Store>();

		stores.add(new DefaultingStore(PreferencesStore.user(),
				new PropertiesStore(getClass(), "preferences.properties")));

		return stores;
	}
}