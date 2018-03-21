import React from 'react';
import SidebarWrapper from './SC/SidebarWrapper'
function round(number, precision) {
    var factor = Math.pow(10, precision);
    return Math.round(number * factor) / factor;
  }

const Sidebar = ({ data, handleDatasetClick, selected }) => {
    const dataSets = data.map((dataset, index) => {
        return (
            <div
                className={ selected === dataset.name ? "selected dataset" : "dataset"}
                key={index}
                onClick={handleDatasetClick.bind(this, dataset.name)}
            >
                <h3>{dataset.name}</h3>
                <p>Prediction Accuracy: {round(dataset.accuracy,3)}</p>
                <p>Images Count: {dataset.imageCount}</p>
            </div>
        )

    })
    return (
        <SidebarWrapper>
            <h1>Choose a dataset:</h1>
            {
                dataSets
            }
        </SidebarWrapper>
    );
};

export default Sidebar;