import React from 'react';

const Dialog = ({isCat}) => {
    return (
        <div style={{
            position:'fixed',
            top: 0, bottom: 0, left: 0, right: 0,
            display:'flex', alignItems:'center', justifyContent:'center',
            backgroundColor: 'rgba(20, 20, 20, .8)'
            }}>
            <h1 style={{alignSelf:'center', color: 'white'}}>
            {isCat ? "That's a cat. Good job!" : "I don't think that's a cat..." }
            </h1>
        </div>
    );
};

export default Dialog;